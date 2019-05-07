package com.cloud.controller.file;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cloud.entity.CloudFile;
import com.cloud.entity.CloudFileMapper;
import com.cloud.entity.Folder;
import com.cloud.entity.FolderMapper;
import com.cloud.entity.User;
import com.post.util.MyBatisUtil;
import com.post.util.SystemConstant;
import com.post.util.ZipCompressor;

@Controller
@RequestMapping(value = "/main")
public class FileManage {
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String FileUpload(@RequestParam("dataList") MultipartFile[] fileList, @RequestParam("path") Integer folderId,
			Model model, HttpSession session, HttpServletRequest request) {
		String sysMsg = "";
		if (fileList.length == 0) {
			model.addAttribute("请选择文件");
			System.out.println("1:"+folderId);
			return "/main/file?dir=" + folderId;
		}
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		try {
			User user = (User) session.getAttribute("user");

			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			Folder folder;
			if (folderId == 0)
				folder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				folder = folderMapper.selectFolderById(folderId);

			String uploadPath = SystemConstant.USER_FILE_PATH + folder.getPath() + "\\" + folder.getName() + "\\";
			File file=new File(uploadPath);    
			if(!file.exists())
				file.mkdir();
			for (MultipartFile files : fileList) {
				String name = files.getOriginalFilename();
				if (name.equals("")) {
					sysMsg = "请选择文件";
					return "/main/file?dir=" + folderId;
				}
				int size = (int) (files.getSize() / 1024);
				if (size == 0) {
					size = 1;
					}

				int sub = files.getOriginalFilename().lastIndexOf('.');

				Map<String, Object> params = new HashMap<>();
				params.put("fileName", name);
				params.put("userId", user.getId());
				params.put("dirId", folder.getId());

				CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);

				Integer sameNamefile = cloudFileMapper.selectFileByFileName(params).size();

				String filetype = files.getOriginalFilename().substring(sub + 1).toLowerCase();
				if (sameNamefile > 0) {
					name = name.replaceAll("." + filetype, "") + "(" + sameNamefile + ")" + "." + filetype;
				}
				CloudFile cloudFile = new CloudFile(name, size, filetype, folder, user);

				cloudFileMapper.addFile(cloudFile);

				File uploadFile = new File(uploadPath, name);
				files.transferTo(uploadFile);

				String urll = request.getSession().getServletContext().getRealPath("") + "\\upload";
				File uploadFile2 = new File(urll, name);

				FileUtils.copyFile(uploadFile, uploadFile2);

			}
			sqlSession.commit();
			sysMsg = "文件上传成功";

		} catch (Exception e) {
			sysMsg = "请选择文件";
			sqlSession.rollback();
			e.printStackTrace();
		} finally {
			model.addAttribute("sysMsg", sysMsg);
			sqlSession.close();
		}
		
		return "/main/file?dir=" + folderId;
	}

	@RequestMapping(value = "/folderAdd")
	public String addFolder(@RequestParam("folderName") String folderName, @RequestParam("path") String path,
			HttpSession session, HttpServletRequest request, Model model) {
		if (folderName == null || folderName.equals("")) {
			model.addAttribute("sysMsg", "请输入文件夹名");
			return "/main/file?dir=0";
		}
		if (!SystemConstant.isValidFileName(folderName)) {
			model.addAttribute("sysMsg", "文件夹名不规范，请重新输入");
			return "/main/file?dir=0";
		}
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		String sysMsg = "";
		Integer folderId = Integer.valueOf(path);
		try {
			User user = (User) session.getAttribute("user");
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);

			Folder fatherFolder, newFolder;
			if (folderId == 0)
				fatherFolder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				fatherFolder = folderMapper.selectFolderById(folderId);

			newFolder = new Folder(folderName, fatherFolder.getId(), user,
					fatherFolder.getPath() + fatherFolder.getName() + "\\");

			String realPath = SystemConstant.USER_FILE_PATH;
			File realFolder = new File(realPath + fatherFolder.getPath() + fatherFolder.getName() + "\\" + folderName);
			if (!realFolder.exists())
				realFolder.mkdirs();

			folderMapper.addFolder(newFolder);
			sqlSession.commit();
			sysMsg = "上传成功";
		} catch (Exception e) {
			sysMsg = "系统异常";
			e.printStackTrace();
		} finally {
			model.addAttribute("sysMsg", sysMsg);
			sqlSession.close();
		}

		return "/main/file?dir=" + folderId;
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ResponseEntity<byte[]> fileDownload(@RequestParam("file") Integer fileId,
			@RequestParam("folder") Integer folderId, HttpSession session) {
		System.out.println("downloading start..." + fileId);
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		ResponseEntity<byte[]> responseEntity = null;
		try {
			User user = (User) session.getAttribute("user");
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);

			Folder folder;
			if (folderId == null || folderId == 0)
				folder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				folder = folderMapper.selectFolderById(folderId);
			CloudFile cloudFile = cloudFileMapper.selectFileById(fileId);
			if (cloudFile.getUploadUser().getId() != user.getId()
					&& cloudFile.getStatus() == CloudFile.STATUS_PRIVATE) {
				sqlSession.close();
				return null;
			}

			String path = SystemConstant.USER_FILE_PATH + folder.getPath() + folder.getName() + "\\"
					+ cloudFile.getName();

			File file = new File(path);
			HttpHeaders headers = new HttpHeaders();
			String fileName = new String(cloudFile.getName().getBytes("UTF-8"), "ISO-8859-1");
			headers.setContentDispositionFormData("attachment", fileName);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			responseEntity = new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers,
					HttpStatus.CREATED);
		} catch (Exception e) {
			sqlSession.rollback();
			e.printStackTrace();
		} finally {
			sqlSession.close();
			System.out.println("downloading end..." + fileId);
		}
		return responseEntity;
	}

	@RequestMapping(value = "/copy", method = RequestMethod.GET)
	public String fileCopy(@RequestParam("file") Integer fileId, @RequestParam("folder") Integer folderId, Model model,
			HttpSession session) {
		System.out.println("copy start..." + fileId);
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		try {
			User user = (User) session.getAttribute("user");
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);

			Folder folder;
			if (folderId == 0)
				folder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				folder = folderMapper.selectFolderById(folderId);
			CloudFile cloudFile = cloudFileMapper.selectFileById(fileId);
			if (cloudFile.getUploadUser().getId() != user.getId()
					&& cloudFile.getStatus() == CloudFile.STATUS_PRIVATE) {
				sqlSession.close();
				return null;
			}

			String path = SystemConstant.USER_FILE_PATH + folder.getPath() + folder.getName() + "\\"
					+ cloudFile.getName();
			String copypath = SystemConstant.USER_FILE_PATH + folder.getPath() + folder.getName() + "\\"
					+ cloudFile.getName().replaceAll("." + cloudFile.getFileType(), "") + "_Copy."
					+ cloudFile.getFileType();

			FileUtils.copyFile(new File(path), new File(copypath));

			CloudFile cloudFile1 = new CloudFile();
			cloudFile1.setName(cloudFile.getName().replaceAll("." + cloudFile.getFileType(), "") + "_Copy."
					+ cloudFile.getFileType());
			cloudFile1.setFatherFolder(cloudFile.getFatherFolder());
			cloudFile1.setFileType(cloudFile.getFileType());
			cloudFile1.setSize(cloudFile.getSize());
			cloudFile1.setStatus(cloudFile.getStatus());
			cloudFile1.setUploadTime(cloudFile.getUploadTime());
			cloudFile1.setUploadUser(cloudFile.getUploadUser());

			cloudFileMapper.addFile(cloudFile1);
			sqlSession.commit();
		} catch (Exception e) {
			sqlSession.rollback();
			e.printStackTrace();
		} finally {
			sqlSession.close();
			System.out.println("copy end..." + fileId);
			model.addAttribute("sysMsg", "文件拷贝成功！");
		}
		return "/main/file?dir=" + folderId;
	}

	@RequestMapping(value = "/downloads", method = RequestMethod.GET)
	public ResponseEntity<byte[]> fileDownloads(@RequestParam("file") String fileIds,
			@RequestParam("folder") String folderIds, HttpSession session) throws Exception {
		ResponseEntity<byte[]> responseEntity;
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		List<String> filePaths = new ArrayList<String>();
		for (String tmpfileId : fileIds.split(",")) {
			if ("".equals(tmpfileId)) {
				continue;
			}
			Integer fileId = Integer.valueOf(tmpfileId);
			Integer folderId = Integer.valueOf(folderIds.split(",")[0]);

			try {
				User user = (User) session.getAttribute("user");
				FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
				CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);

				Folder folder;
				if (folderId == 0)
					folder = folderMapper.selectRootFolderByUserId(user.getId());
				else
					folder = folderMapper.selectFolderById(folderId);
				CloudFile cloudFile = cloudFileMapper.selectFileById(fileId);
				if (cloudFile.getUploadUser().getId() != user.getId()
						&& cloudFile.getStatus() == CloudFile.STATUS_PRIVATE) {
					sqlSession.close();
					return null;
				}

				String path = SystemConstant.USER_FILE_PATH + folder.getPath() + folder.getName() + "\\"
						+ cloudFile.getName();
				filePaths.add(path);

			} catch (Exception e) {
				sqlSession.rollback();
				e.printStackTrace();
			}

		}

		String zipFilePath = System.getProperty("user.dir") + File.separator + "Files.zip";
		System.out.println("zip file path:" + zipFilePath);
		ZipCompressor zc = new ZipCompressor(zipFilePath);

		zc.compress(filePaths);

		HttpHeaders headers = new HttpHeaders();
		String fileName = new String("Files.zip".getBytes("UTF-8"), "ISO-8859-1");
		headers.setContentDispositionFormData("attachment", fileName);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		responseEntity = new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(new File(zipFilePath)), headers,
				HttpStatus.CREATED);

		return responseEntity;
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public String fileDelete(@RequestParam("file") Integer fileId, @RequestParam("folder") Integer folderId,
			HttpSession session, Model model) {
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		String sysMsg = "";
		Folder folder;
		User user = (User) session.getAttribute("user");
		FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
		CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);

		if (folderId == null || folderId == 0)
			folder = folderMapper.selectRootFolderByUserId(user.getId());
		else {
			folder = folderMapper.selectFolderById(folderId);
		}
		try {
			cloudFileMapper.deleteFileById(fileId);
			sqlSession.commit();
			sysMsg = "删除成功";
		} catch (Exception e) {
			sqlSession.rollback();
			sysMsg = "系统异常";
			e.printStackTrace();
		} finally {
			model.addAttribute("sysMsg", sysMsg);
			sqlSession.close();
		}

		return "/main/file?dir=" + folder.getId();
	}

	@RequestMapping(value = "/deleteDir", method = RequestMethod.GET)
	public String folderDelete(@RequestParam("folder") Integer folderId, Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		String sysMsg = "";
		try {
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);

			Folder folder = folderMapper.selectFolderById(folderId);
			if (folder.getCreateUser().getId() != user.getId()) {
				model.addAttribute("sysMsg", "你没有权限删除该文件夹");
				sqlSession.close();
				return "/index.jsp";
			}
			File target = new File(SystemConstant.USER_FILE_PATH + "\\" + folder.getPath() + folder.getName());

			folder.setPath(SystemConstant.toSQLString(folder.getPath()));
			List<Folder> targetFolder = folderMapper.selectAllFolderByFatherFolder(folder);
			for (Folder f : targetFolder) {
				folderMapper.deleteFolderById(f.getId());
			}
			folderMapper.deleteFolderById(folderId);
			sysMsg = "删除成功";
		} catch (Exception e) {
			sysMsg = "系统异常";
			sqlSession.rollback();
			e.printStackTrace();
		} finally {
			model.addAttribute("sysMsg", sysMsg);
			sqlSession.close();
		}
		return "/main/file?dir=0";
	}

	@RequestMapping("/setAccess")
	public String accessFile(@RequestParam("folder") Integer folderId, @RequestParam("file") Integer fileId,
			Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user == null) {
			model.addAttribute("sysMsg", "请重新登录");
			return "/index.jsp";
		}

		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		String sysMsg = "";
		try {
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);

			Folder folder;
			if (folderId == 0)
				folder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				folder = folderMapper.selectFolderById(folderId);

			CloudFile target = cloudFileMapper.selectFileById(fileId);

			Map<String, Object> params = new HashMap<>();
			if (target.getStatus() == CloudFile.STATUS_PRIVATE) {
				sysMsg = "成功分享该文件";
				params.put("access", CloudFile.STATUS_PUBLIC);
			} else {
				sysMsg = "已取消该文件分享";
				params.put("access", CloudFile.STATUS_PRIVATE);
			}
			params.put("fileId", target.getId());

			cloudFileMapper.changeFileAccessByFileId(params);
			sqlSession.commit();
		} catch (Exception e) {
			sysMsg = "系统异常";
			sqlSession.rollback();
			e.printStackTrace();
		} finally {
			model.addAttribute("sysMsg", sysMsg);
			sqlSession.close();
		}
		return "/main/file?dir=" + folderId;
	}

	@RequestMapping("/outShare")
	public String outShare(@RequestParam("folder") Integer folderId, @RequestParam("file") Integer fileId, Model model,
			HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user == null) {
			model.addAttribute("sysMsg", "请重新登录");
			return "/index.jsp";
		}

		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		String sysMsg = "";
		try {
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);

			Folder folder;
			if (folderId == 0)
				folder = folderMapper.selectRootFolderByUserId(user.getId());
			else
				folder = folderMapper.selectFolderById(folderId);

			CloudFile target = cloudFileMapper.selectFileById(fileId);

			Map<String, Object> params = new HashMap<>();
			if (target.getStatus() == CloudFile.STATUS_PRIVATE) {
				sysMsg = "成功分享该文件";
				params.put("access", CloudFile.STATUS_PUBLIC);
			} else {
				sysMsg = "已取消该文件分享";
				params.put("access", CloudFile.STATUS_PRIVATE);
			}
			params.put("fileId", target.getId());

			cloudFileMapper.changeFileAccessByFileId(params);
			sqlSession.commit();

			String outurl = "http://localhost:8080/20151104806_chengguohui_cloudStorage/main/download?file=" + fileId + "&folder=" + folderId;
			model.addAttribute("sysMsg", "外链分享成功，URL：" + outurl);
		}

		catch (Exception e) {
			sysMsg = "系统异常";
			sqlSession.rollback();
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return "/main/file?dir=" + folderId;
	}

	@RequestMapping(value = "/restore", method = RequestMethod.GET)
	public String restore(@RequestParam("fileId") Integer fileId, @RequestParam("folder") Integer folderId, Model model,
			HttpSession session) {
		System.out.println("copy start..." + fileId);
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		try {
			User user = (User) session.getAttribute("user");
			FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
			CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);
			if (!StringUtils.isEmpty(fileId)) {
				cloudFileMapper.restoreFile(fileId);
			}
			if (!StringUtils.isEmpty(folderId)) {
				cloudFileMapper.restoreFolder(folderId);
			}
			sqlSession.commit();
		} catch (Exception e) {
			sqlSession.rollback();
			e.printStackTrace();
		} finally {
			sqlSession.close();
			System.out.println("copy end..." + fileId);
			model.addAttribute("sysMsg", "文件恢复成功！");
		}
		return "/main/file?dir=" + folderId;
	}
	
	@RequestMapping(value = "/deletetrue", method = RequestMethod.GET)
	public String deletetrue(@RequestParam("fileId") Integer fileId, @RequestParam("folder") Integer folderId, Model model,
			HttpSession session) {
		System.out.println("copy start..." + fileId);
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);
		FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
		Folder folder;
		
		try {
			User user = (User) session.getAttribute("user");
			if (folderId == null || folderId == 0) {
				folder = folderMapper.selectRootFolderByUserId(user.getId());
			} else {
				folder = folderMapper.selectFolderById(folderId);
			}
			
            CloudFile cloudFile = cloudFileMapper.selectDeletedFileById(fileId);
            String path = SystemConstant.USER_FILE_PATH + folder.getPath() + folder.getName() + "\\" + cloudFile.getName();
            cloudFileMapper.deletetrueFileById(fileId);
			
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
			sqlSession.commit();
		} catch (Exception e) {
			sqlSession.rollback();
			e.printStackTrace();
		} finally {
			sqlSession.close();
			System.out.println("copy end..." + fileId);
			model.addAttribute("sysMsg", "文件清空成功！");
		}
		return "/main/file?dir=" + folderId;
	}

	@SuppressWarnings("Duplicates")
	@RequestMapping(value = "/confirmDirDeletion", method = RequestMethod.GET)
	public String confirmDirDeletion(@RequestParam("folder") Integer folderId, Model model, HttpSession session) {
        if (StringUtils.isEmpty(folderId)) {
            model.addAttribute("sysMsg", "所选的文件夹为空！");
            return "/main/file?dir=" + folderId;
        }
		SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
		CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);
		FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
		Folder folder;

		try {
			folder = folderMapper.selectFolderById(folderId);
			String path = SystemConstant.USER_FILE_PATH + folder.getPath() + folder.getName() + "\\" ;

            folderMapper.deleteFolderByIdFromRecycleBin(folderId);
            cloudFileMapper.deleteTrueFileByFolderId(folderId);

			File directory = new File(path);
			if (!directory.exists()) {
                throw new RuntimeException("文件夹不存在");
			}

            if (!SystemConstant.deleteDir(directory)) {
                throw new RuntimeException("文件夹删除失败");
            }
            sqlSession.commit();
            model.addAttribute("sysMsg", "文件清空成功！");
		} catch (Exception e) {
			sqlSession.rollback();
			e.printStackTrace();
            model.addAttribute("sysMsg", "文件删除失败");
		} finally {
			sqlSession.close();
		}
		return "/main/file?dir=" + folderId;
	}

    @RequestMapping(value = "/move", method = RequestMethod.GET)
    public String fileMove(@RequestParam("file") Integer fileId, @RequestParam("folderName") String folderName,
                           @RequestParam("path") String path, Model model,
                           HttpSession session, HttpServletRequest request,
                           HttpServletResponse response) throws UnsupportedEncodingException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession(true);
        try {
            User user = (User) session.getAttribute("user");
            FolderMapper folderMapper = sqlSession.getMapper(FolderMapper.class);
            CloudFileMapper cloudFileMapper = sqlSession.getMapper(CloudFileMapper.class);

            Integer folderId = Integer.valueOf(path);

            Folder fatherFolder, newFolder;
            if (folderId == 0)
                fatherFolder = folderMapper.selectRootFolderByUserId(user.getId());
            else
                fatherFolder = folderMapper.selectFolderById(folderId);

            newFolder = new Folder(folderName, fatherFolder.getId(), user,
                    fatherFolder.getPath() + fatherFolder.getName() + "\\");

            String realPath = SystemConstant.USER_FILE_PATH;
            File realFolder = new File(realPath + fatherFolder.getPath() + fatherFolder.getName() + "\\" + folderName);
            if (!realFolder.exists()) {
                realFolder.mkdirs();
                folderMapper.addFolder(newFolder);
            }


            Folder tmpFolder = new Folder();
            tmpFolder.setCreateUser(user);
            tmpFolder.setName(folderName);
            Folder dirId = folderMapper.selectNewestFolder(tmpFolder);
            newFolder.setId(dirId.getId());

            Folder folder;
            if (folderId == 0) {
                folder = folderMapper.selectRootFolderByUserId(user.getId());
            } else
                folder = folderMapper.selectFolderById(folderId);
            CloudFile cloudFile = cloudFileMapper.selectFileById(fileId);
            if (cloudFile.getUploadUser().getId() != user.getId()
                    && cloudFile.getStatus() == CloudFile.STATUS_PRIVATE) {
                sqlSession.close();
                return null;
            }

            String path2 = SystemConstant.USER_FILE_PATH + folder.getPath() + folder.getName() + "\\"
                    + cloudFile.getName();

            FileUtils.moveFileToDirectory(new File(path2), realFolder, false);

            CloudFile cloudFile1 = new CloudFile();
            cloudFile1.setName(cloudFile.getName());
            cloudFile1.setFatherFolder(newFolder);
            cloudFile1.setFileType(cloudFile.getFileType());
            cloudFile1.setSize(cloudFile.getSize());
            cloudFile1.setStatus(cloudFile.getStatus());
            cloudFile1.setUploadTime(cloudFile.getUploadTime());
            cloudFile1.setUploadUser(cloudFile.getUploadUser());

            cloudFileMapper.addFile(cloudFile1);

            cloudFileMapper.deletetrueFileById(fileId);
            sqlSession.commit();
            model.addAttribute("sysMsg", "文件移动成功！");
        } catch (Exception e) {
            sqlSession.rollback();
            e.printStackTrace();
            model.addAttribute("sysMsg", "文件移动失败！");
        } finally {
            sqlSession.close();
            System.out.println("copy end..." + fileId);
        }
        return "/main/file?dir=" + 0;
    }
}