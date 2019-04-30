package com.cloud.entity;

import java.util.List;
import java.util.Map;

public interface CloudFileMapper
{
	public void addFile(CloudFile file);
	
	public List<CloudFile> selectFileListByFolderId(Integer folderId);
	
	public CloudFile selectFileById(Integer fileId);
	
	public CloudFile selectDeletedFileById(Integer fileId);
	
	public List<CloudFile> selectPublicFile();
	
	public List<CloudFile> selectFileByFileName(Map<String,Object> params);
	
	public void deleteFileById(Integer fileId);
	
	public void deleteFileByFolderId(Integer folderId);

	public void deleteTrueFileByFolderId(Integer folderId);
	 
	public List<CloudFile> selectPhotoByUserId(Integer userId);
	
	public List<CloudFile> selectDocumentByUserId(Integer userId);
	
	public List<CloudFile> selectMovieByUserId(Integer userId);
	
	public List<CloudFile> selectMusicByUserId(Integer userId);
	
	public List<CloudFile> selectZipByUserId(Integer userId);
	
	public void changeFileAccessByFileId(Map<String, Object> params);

	public List<CloudFile> selectRecycleBinFile();

	public void restoreFile(Integer fileId);
	
	public void restoreFolder(Integer folderId);

	public void deletetrueFileById(Integer fileId);
}
