package com.storyloom.archive;

import com.storyloom.archive.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {

    @Mock
    private MinioClient minioClient; 

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService("http://dummy-url", "accessKey", "secretKey");
        ReflectionTestUtils.setField(fileStorageService, "minioClient", minioClient);
        ReflectionTestUtils.setField(fileStorageService, "bucketName", "test-bucket");
    }

    @Test
    void saveFile_WithEmptyFile_ShouldReturnNull() {
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        String result = fileStorageService.saveFile(emptyFile);
        assertNull(result, "Service should return null for an empty file");
    }

    @Test
    void saveFile_WithValidFile_ShouldReturnUuidFilename() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "beowulf.txt", "text/plain", "Hello World".getBytes());

        String result = fileStorageService.saveFile(validFile);

        assertNotNull(result);
        assertTrue(result.endsWith(".txt"), "Filename should retain its extension");
        assertNotEquals("beowulf.txt", result, "Filename should be randomized using UUID");
        verify(minioClient, times(1)).putObject(ArgumentMatchers.any(PutObjectArgs.class));
    }

    @Test
    void saveFile_WhenMinioFails_ShouldThrowRuntimeException() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello".getBytes());
                
        when(minioClient.putObject(ArgumentMatchers.any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("MinIO Server Down"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.saveFile(validFile);
        });
        
        assertTrue(exception.getMessage().contains("Failed to store file"));
    }
}