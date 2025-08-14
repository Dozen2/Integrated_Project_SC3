package sit.int221.sc3_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.sc3_server.configuration.FileStorageProperties;

import java.io.IOException;
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

    public String store(MultipartFile file){
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.contains("..")){
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            if(!isSupportedContentType(file)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Does not support content type: " + file.getContentType());
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        }catch (IOException exception){
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", exception);
        }
    }

    private boolean isSupportedContentType(MultipartFile file) {
        String contentType = file.getContentType();
        Set<String> allowTypes = Arrays.stream(fileStorageProperties.getAllowFileType()).collect(Collectors.toSet());
        return allowTypes.contains(contentType);
    }
    public List<String> storeList(List<MultipartFile> files) {
        List<String> fileNames = new ArrayList<>(files.size());
        files.forEach(file -> fileNames.add(store(file)));
        return fileNames;
    }

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


}
