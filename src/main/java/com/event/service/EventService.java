package com.event.service;

import java.util.List;

public interface EventService {

	/**
	 * 이벤트 첨부파일을 event 접두사로 업로드하고 파일 정보를 등록한다.
	 * 반환된 파일 ID는 이벤트 수정 시 첨부 ID로 저장한다.
	 *
	 * @param files 업로드할 첨부파일 목록
	 * @param description 파일 설명
	 * @return 등록된 파일 정보 목록
	 * @throws IOException 첨부파일을 읽거나 저장하는 중 오류가 발생한 경우
	 */
	List<FileVO> uploadEventFiles(List<MultipartFile> files, String description) throws IOException;


	EventVO selectEvent(EventVO eventVO) throws Exception;

	List<EventVO> selectEventList(EventFilterVO filterVO) throws Exception;

	int selectEventListCnt(EventFilterVO filterVO) throws Exception;

	void insertEvent(EventVO eventVO) throws Exception;

	void updateEvent(EventVO eventVO) throws Exception;

	void deleteEvent(EventVO eventVO) throws Exception;
}