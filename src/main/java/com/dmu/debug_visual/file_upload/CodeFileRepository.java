package com.dmu.debug_visual.file_upload;


import com.dmu.debug_visual.file_upload.entity.CodeFile;
import com.dmu.debug_visual.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodeFileRepository extends JpaRepository<CodeFile, Long> {
    Optional<CodeFile> findByFileUUID(String fileUUID);
    List<CodeFile> findByUser(User user); // '/api/files/my' 기능을 위해
}