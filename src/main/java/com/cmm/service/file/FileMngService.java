package com.cmm.service.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드, 조회, 수정, 삭제 및 다운로드를 위한 공통 파일 관리 서비스이다.
 */
public interface FileMngService {

    /**
     * 현재 설정된 파일 저장소의 연결 상태를 확인한다.
     */
    void checkStorageConnection();

    /**
     * 여러 파일을 업로드하고 각 파일의 정보를 등록한다.
     *
     * @param files 업로드할 파일 목록
     * @param description 파일 설명
     * @return 등록된 파일 정보 목록
     * @throws IOException 파일을 읽거나 저장하는 중 오류가 발생한 경우
     */
    List<FileVO> uploadFiles(List<MultipartFile> files, String description) throws IOException;

    /**
     * 하나의 파일에 대한 정보를 수정하고, 교체 파일이 있으면 파일 실물도 교체한다.
     * 파일을 교체해도 기존 파일 ID는 유지한다.
     *
     * @param fileId 수정할 파일 ID
     * @param replacement 교체할 파일. null이면 기존 파일 실물을 유지한다.
     * @param description 파일 설명. null이면 기존 설명을 유지한다.
     * @return 수정된 파일 정보
     * @throws IOException 교체 파일을 읽거나 저장하는 중 오류가 발생한 경우
     */
    FileVO updateFile(Long fileId, MultipartFile replacement, String description) throws IOException;

    /**
     * 등록된 파일 정보를 확인하고 다운로드를 위한 파일 스트림을 연다.
     * 반환된 스트림은 호출한 곳에서 사용 후 닫아야 한다.
     *
     * @param file 조회한 파일 정보
     * @return 다운로드할 파일의 입력 스트림
     * @throws IOException 파일 스트림을 여는 중 오류가 발생한 경우
     */
    InputStream openFile(FileVO file) throws IOException;

    /**
     * 검색 조건에 해당하는 파일 목록을 조회한다.
     *
     * @param filter 파일 검색 조건
     * @return 파일 정보 목록
     */
    List<FileVO> selectFileInfs(FileVO filter);

    /**
     * 하나의 파일에 대한 상세정보를 조회한다.
     *
     * @param file 조회할 파일 ID가 담긴 파일 정보
     * @return 파일 상세정보
     */
    FileVO selectFileInf(FileVO file);

    /**
     * 저장소에 이미 저장된 하나의 파일에 대한 정보를 등록한다.
     * 파일 실물 업로드는 uploadFiles를 사용한다.
     *
     * @param file 저장된 파일의 정보
     * @return 생성된 파일 ID
     * @throws IOException 저장소에 파일이 존재하는지 확인하는 중 오류가 발생한 경우
     */
    String insertFileInf(FileVO file) throws IOException;

    /**
     * 저장소에 이미 저장된 여러 파일에 대한 정보를 등록한다.
     * 각 파일은 독립된 파일 ID를 갖는다.
     *
     * @param files 저장된 파일 정보 목록
     * @return 생성된 파일 ID를 쉼표로 구분한 문자열
     * @throws IOException 저장소에 파일이 존재하는지 확인하는 중 오류가 발생한 경우
     */
    String insertFileInfs(List<FileVO> files) throws IOException;

    /**
     * 여러 파일에 대한 설명을 수정한다.
     *
     * @param files 수정할 파일 ID와 설명이 담긴 파일 정보 목록
     */
    void updateFileInfs(List<FileVO> files);

    /**
     * 하나의 파일 정보를 삭제하고, 트랜잭션 확정 후 저장소의 파일 실물을 삭제한다.
     *
     * @param file 삭제할 파일 ID가 담긴 파일 정보
     */
    void deleteFileInf(FileVO file);

    /**
     * 여러 파일 정보를 삭제하고, 트랜잭션 확정 후 저장소의 파일 실물을 삭제한다.
     *
     * @param files 삭제할 파일 ID가 담긴 파일 정보 목록
     */
    void deleteFileInfs(List<FileVO> files);

    /**
     * 파일명 검색 조건에 해당하는 파일 목록을 조회한다.
     *
     * @param filter 파일명 검색 조건
     * @return items에 파일 목록이 담긴 결과
     */
    Map<String, Object> selectFileListByFileNm(FileVO filter);

    /**
     * 검색 조건에 해당하는 이미지 파일 목록을 조회한다.
     * 이미지 확장자는 gif, jpg, jpeg, png를 대상으로 한다.
     *
     * @param filter 파일 검색 조건
     * @return 이미지 파일 정보 목록
     */
    List<FileVO> selectImageFileList(FileVO filter);
}
