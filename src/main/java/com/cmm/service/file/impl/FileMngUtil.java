package com.cmm.service.file.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cmm.service.file.FileMngService;
import com.cmm.service.file.FileVO;
import com.cmm.storage.FileStorage;

//파일 확장자를 추출한다.
//파일 확장자의 허용유무를 검증한다.
//최대 파일 사이즈 허용유무를 검증한다.