package com.ailearning.courseservice.service;

import com.ailearning.courseservice.dto.request.CreateSubjectRequest;
import com.ailearning.courseservice.dto.request.UpdateSubjectRequest;
import com.ailearning.courseservice.dto.response.SubjectResponse;

import java.util.List;

public interface SubjectService {

    SubjectResponse createSubject(CreateSubjectRequest request);

    List<SubjectResponse> getAllSubjects();

    SubjectResponse getSubjectById(Long subjectId);

    SubjectResponse updateSubject(Long subjectId,
                                  UpdateSubjectRequest request);

    void deleteSubject(Long subjectId);

}