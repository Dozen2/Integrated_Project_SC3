package sit.int221.sc3_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.sc3_server.configuration.FileStorageProperties;
import sit.int221.sc3_server.repository.SaleItemImageRepository;
import sit.int221.sc3_server.repository.SaleitemRepository;

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
    @Autowired
    private FileStorageProperties fileStorageProperties;
    @Autowired
    private SaleItemImageRepository saleItemImageRepository;

    public FileService(FileStorageProperties fileStorageProperties){
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try{
            if (!Files.exists(this.fileStorageLocation)){
                Files.createDirectories(this.fileStorageLocation);
            }
        }catch (Exception e){
            throw new RuntimeException("Can’t create the directory where the uploaded files will be stored", e);
        }
    }

    public String store(MultipartFile file,String newFileName){
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String newFile = StringUtils.cleanPath(newFileName);
        try {
            if (fileName.contains("..")){
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + newFile);
            }
            if(!isSupportedContentType(file)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Does not support content type: " + file.getContentType());
            }

            Path targetLocation = this.fileStorageLocation.resolve(newFile);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return newFile;
        }catch (IOException exception){
            throw new RuntimeException("Could not store file " + newFile + ". Please try again!", exception);
        }
    }
//    public String store(MultipartFile file, String newFileName) {
//        String fileName = StringUtils.cleanPath(newFileName); // ใช้ชื่อไฟล์ใหม่ที่สุ่มมา
//        try {
//            if (fileName.contains("..")) {
//                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
//            }
//            if (!isSupportedContentType(file)) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                        "Does not support content type: " + file.getContentType());
//            }
//
//            Path targetLocation = this.fileStorageLocation.resolve(fileName);
//            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
//            return fileName;
//        } catch (IOException exception) {
//            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", exception);
//        }
//    }


    private boolean isSupportedContentType(MultipartFile file) {
        String contentType = file.getContentType();
        Set<String> allowTypes = Arrays.stream(fileStorageProperties.getAllowFileType()).collect(Collectors.toSet());
        return allowTypes.contains(contentType);
    }
//    public List<String> storeList(List<MultipartFile> files) {
//        List<String> fileNames = new ArrayList<>(files.size());
//        files.forEach(file -> fileNames.add(store(file)));
//        return fileNames;
//    }

    public List<String> getMatchedFiles(String pattern) {
        List<String> matchesList = new ArrayList<String>();
        FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attribs)
                    throws IOException {
                FileSystem fs = FileSystems.getDefault();
                PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);
                Path name = file.getFileName();
                if (matcher.matches(name)) {
                    matchesList.add(name.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(this.fileStorageLocation, matcherVisitor);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return matchesList;
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
}
