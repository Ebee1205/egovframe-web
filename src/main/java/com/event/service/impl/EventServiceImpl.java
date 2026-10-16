package com.event.service.impl;

import java.util.List;
import java.math.BigDecimal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;
import com.cmm.service.file.FileMngService;
import com.cmm.service.file.FileVO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.service.EventFilterVO;
import com.event.service.EventService;
import com.event.service.EventVO;

@Service
public class EventServiceImpl implements EventService {

	private final EventDAO eventDAO;
	private final EventCmtDAO eventCmtDAO;
	private final FileMngService fileMngService;

	public EventServiceImpl(EventDAO eventDAO, EventCmtDAO eventCmtDAO, FileMngService fileMngService) {
		this.eventDAO = eventDAO;
		this.eventCmtDAO = eventCmtDAO;
		this.fileMngService = fileMngService;
	}

	@Override
	public EventVO selectEvent(EventVO eventVO) throws Exception {
		EventVO event = eventDAO.selectEvent(eventVO);
		if (event != null) {
			event.setAttachedFiles(eventDAO.selectEventFiles(event.getEid()));
		}
		return event;
	}

	@Override
	public List<EventVO> selectEventList(EventFilterVO filterVO) throws Exception {
		if (filterVO.getPageIndex() < 1) {
			filterVO.setPageIndex(1);
		}
		filterVO.setFirstIndex(filterVO.getPageSize() > 0
				? (filterVO.getPageIndex() - 1) * filterVO.getPageSize() : 0);
		filterVO.setTotalCnt(selectEventListCnt(filterVO));
		filterVO.setLastIndex(filterVO.getPageSize() > 0
				? filterVO.getFirstIndex() + filterVO.getPageSize() : filterVO.getTotalCnt());
		return eventDAO.selectEventList(filterVO);
	}

	@Override
	public int selectEventListCnt(EventFilterVO filterVO) throws Exception {
		return eventDAO.selectEventListCnt(filterVO);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insertEvent(EventVO eventVO) throws Exception {
		prepareEventGeo(eventVO, null);
		eventDAO.insertEvent(eventVO);
		insertEventFiles(eventVO);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateEvent(EventVO eventVO) throws Exception {
		EventVO current = eventDAO.selectEventForGeoUpdate(eventVO);
		if (current == null) {
			throw new IllegalArgumentException("이벤트를 찾을 수 없습니다.");
		}
		prepareEventGeo(eventVO, current);
		eventDAO.updateEvent(eventVO);
		eventDAO.deleteEventFiles(eventVO);
		insertEventFiles(eventVO);
	}

	private void prepareEventGeo(EventVO eventVO, EventVO current) throws Exception {
		BigDecimal lat = eventVO.getLat();
		BigDecimal lon = eventVO.getLon();
		// Never trust a form-supplied gid. Missing coordinates preserve the stored link.
		eventVO.setGid(current == null ? null : current.getGid());
		if (lat == null && lon == null) {
			return;
		}
		if (lat == null || lon == null) {
			throw new IllegalArgumentException("위도와 경도를 함께 입력해주세요.");
		}
		if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0
				|| lon.compareTo(BigDecimal.valueOf(-180)) < 0 || lon.compareTo(BigDecimal.valueOf(180)) > 0) {
			throw new IllegalArgumentException("위도는 -90~90, 경도는 -180~180 범위여야 합니다.");
		}
		lat = lat.setScale(7, java.math.RoundingMode.HALF_UP);
		lon = lon.setScale(7, java.math.RoundingMode.HALF_UP);
		eventVO.setLat(lat);
		eventVO.setLon(lon);
		if (current != null && current.getGid() != null && current.getLat() != null && current.getLon() != null
				&& lat.compareTo(current.getLat()) == 0 && lon.compareTo(current.getLon()) == 0) {
			return;
		}
		// A geo row can be shared: allocate a new point instead of moving other events.
		eventDAO.insertEventGeo(eventVO);
	}

	@Override
	public List<FileVO> uploadEventFiles(List<MultipartFile> files, String description) throws IOException {
		return fileMngService.uploadFiles(files, description, "event");
	}
	
	private void insertEventFiles(EventVO eventVO) throws Exception {
		List<Long> fileIds = eventVO.getFileIds();
		if (fileIds == null || fileIds.isEmpty()) {
			return;
		}
		Set<Long> uniqueFileIds = new LinkedHashSet<Long>();
		for (Long fileId : fileIds) {
			if (fileId != null) {
				uniqueFileIds.add(fileId);
			}
		}
		if (!uniqueFileIds.isEmpty()) {
			eventVO.setFileIds(new ArrayList<Long>(uniqueFileIds));
			eventDAO.insertEventFiles(eventVO);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteEvent(EventVO eventVO) throws Exception {
		eventCmtDAO.clearEventCmtParentsByEid(eventVO.getEid());
		eventCmtDAO.deleteEventCmtsByEid(eventVO.getEid());
		eventDAO.deleteEventTags(eventVO);
		eventDAO.deleteEventFiles(eventVO);
		eventDAO.deleteEvent(eventVO);
	}
}
