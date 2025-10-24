package sit.int221.sc3_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.sc3_server.configuration.FileStorageProperties;
import sit.int221.sc3_server.repository.saleItem.SaleItemImageRepository;
import sit.int221.sc3_server.repository.user.SellerRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final Path fileStorageLocation;
    private final Path nationalIdProfilePath;
    @Autowired
    private FileStorageProperties fileStorageProperties;
    @Autowired
    private SaleItemImageRepository saleItemImageRepository;


    public FileService(FileStorageProperties fileStorageProperties){
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        this.nationalIdProfilePath = Paths.get(fileStorageProperties.getUploadIdNationalPhoto()).toAbsolutePath().normalize();
        try{
            if (!Files.exists(this.fileStorageLocation)){
                Files.createDirectories(this.fileStorageLocation);
            }
            if (!Files.exists(this.nationalIdProfilePath)){
                Files.createDirectories(this.nationalIdProfilePath);
            }
        }catch (Exception e){
            throw new RuntimeException("Can’t create the directory where the uploaded files will be stored", e);
        }
    }

    public String store(MultipartFile file,String newFileName,String folderType){
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String newFile = StringUtils.cleanPath(newFileName);
        try {
            if (fileName.contains("..")){
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + newFile);
            }
            if(!isSupportedContentType(file)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Does not support content type: " + file.getContentType());
            }
            Path targetDir;
            switch (folderType.toLowerCase()){
                case "saleitem" ->targetDir = this.fileStorageLocation;
                case "nationalid" ->targetDir = this.nationalIdProfilePath;
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown folder type "+ folderType);
            }
            Files.createDirectories(targetDir);
            Path targetLocation = targetDir.resolve(newFile);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return newFile;
        }catch (IOException exception){
            throw new RuntimeException("Could not store file " + newFile + ". Please try again!", exception);
        }
    }



    private boolean isSupportedContentType(MultipartFile file) {
        String contentType = file.getContentType();
        Set<String> allowTypes = Arrays.stream(fileStorageProperties.getAllowFileType()).collect(Collectors.toSet());
        return allowTypes.contains(contentType);
    }




    public Resource loadFileAsResource(String fileName) {
        try {
            String file01 = saleItemImageRepository.findFileName(fileName);
            Path filePath = this.fileStorageLocation.resolve(file01).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File operation error: "
                    + fileName, ex);
        }
    }


    public String getFileType(Resource resource) {
        try {
            return Files.probeContentType(resource.getFile().toPath());
        } catch (IOException ex) {
            throw new RuntimeException("ProbeContentType error: " + resource,ex);
        }
    }
    public void removeFile(String fileName,String folderType) {
        try {
            Path targetDir;
            switch (folderType.toLowerCase()){
                case "saleitem" -> targetDir = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
                case "nationalid" -> targetDir = Paths.get(fileStorageProperties.getUploadIdNationalPhoto()).toAbsolutePath().normalize();
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown folder type "+ folderType);
            }
            Path filePath = targetDir.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            } else {
                throw new ResourceNotFoundException("File not found " + fileName);
            }
        } catch (IOException ex) {
            throw new RuntimeException("File operation (DELETE) error: " + fileName, ex);
        }
    }
}
