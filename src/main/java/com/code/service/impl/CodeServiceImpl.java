package com.code.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.code.service.CodeService;
import com.code.service.CodeFilterVO;
import com.code.service.CodeDetailVO;

@Service
public class CodeServiceImpl implements CodeService {

	private final CodeDetailDAO codeDAO;
	private final CodeOptionDAO codeOptionDAO;

	public CodeServiceImpl(CodeDetailDAO codeDAO, CodeOptionDAO codeOptionDAO) {
		this.codeDAO = codeDAO;
		this.codeOptionDAO = codeOptionDAO;
	}

	@Override
	public CodeDetailVO selectCode(CodeDetailVO codeVO) throws Exception {
		return codeDAO.selectCode(codeVO);
	}

	@Override
	public List<CodeDetailVO> selectCodeList(CodeDetailVO codeVO) throws Exception {
		if (codeVO.getPageIndex() < 1) {
			codeVO.setPageIndex(1); // 페이지 번호 보정
		}
		codeVO.setFirstIndex(codeVO.getPageSize() > 0
				? (codeVO.getPageIndex() - 1) * codeVO.getPageSize() : 0); // 페이징을 사용하지 않으면 첫 인덱스는 0
		codeVO.setTotalCnt(selectCodeListCnt(codeVO)); // 페이징 처리를 위한 총 건수 조회
		codeVO.setLastIndex(codeVO.getPageSize() > 0
				? codeVO.getFirstIndex() + codeVO.getPageSize() : codeVO.getTotalCnt());
		return codeDAO.selectCodeList(codeVO);
	}

	@Override
	public int selectCodeListCnt(CodeDetailVO codeVO) throws Exception {
		return codeDAO.selectCodeListCnt(codeVO);
	}

	@Override
	public List<CodeFilterVO> selectCmmCodeDetail(CodeFilterVO filterVO) throws Exception {
		return codeOptionDAO.selectCmmCodeDetail(filterVO);
	}

	@Override
	public List<CodeFilterVO> selectCodeRoot() throws Exception {
		return codeOptionDAO.selectCodeRoot(new CodeFilterVO());
	}

	@Override
	public void insertCode(CodeDetailVO codeVO) throws Exception {
		codeDAO.insertCode(codeVO);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateCode(CodeDetailVO codeVO) throws Exception {
		CodeDetailVO current = existingCode(codeVO);
		boolean parentChanged = !java.util.Objects.equals(current.getParentCid(), codeVO.getParentCid());
		if (parentChanged && codeDAO.selectCodeChildCount(current) > 0) {
			throw new CodeService.CodeException(409, "CODE_HAS_CHILDREN", "하위 코드가 있는 코드는 부모를 변경할 수 없습니다.", "parentCid");
		}
		if (codeVO.getParentCid() != null && codeVO.getParentCid().equals(codeVO.getCid())) {
			throw new CodeService.CodeException(400, "CODE_PARENT_INVALID", "자기 자신을 부모 코드로 지정할 수 없습니다.", "parentCid");
		}
		if (parentChanged && codeVO.getParentCid() != null) {
			CodeDetailVO parentQuery = new CodeDetailVO(); parentQuery.setCid(codeVO.getParentCid());
			CodeDetailVO parent = codeDAO.selectCode(parentQuery);
			if (parent == null || parent.getParentCid() != null) {
				throw new CodeService.CodeException(400, "CODE_PARENT_INVALID", "부모 코드에는 최상위 코드를 선택해주세요.", "parentCid");
			}
			codeVO.setLevel(parent.getLevel() + 1);
		} else {
			codeVO.setLevel(parentChanged ? 1 : current.getLevel());
		}
		if (codeDAO.selectDuplicateCodeCount(codeVO) > 0) {
			throw new CodeService.CodeException(409, "CODE_DUPLICATE", "같은 부모 코드 아래에 동일한 코드가 이미 있습니다.", "code");
		}
		codeDAO.updateCode(codeVO);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteCode(CodeDetailVO codeVO) throws Exception {
		CodeDetailVO current = existingCode(codeVO);
		if (codeDAO.selectCodeChildCount(current) > 0) {
			throw new CodeService.CodeException(409, "CODE_IN_USE", "하위 코드가 있는 코드는 삭제할 수 없습니다.", null);
		}
		codeDAO.deleteCode(current);
	}

	private CodeDetailVO existingCode(CodeDetailVO query) throws Exception {
		if (query == null || query.getCid() == null || query.getCid() <= 0) {
			throw new CodeService.CodeException(400, "CODE_ID_INVALID", "코드 ID를 확인해주세요.", "cid");
		}
		CodeDetailVO current = codeDAO.selectCodeForUpdate(query);
		if (current == null) {
			throw new CodeService.CodeException(404, "CODE_NOT_FOUND", "코드를 찾을 수 없습니다.", null);
		}
		return current;
	}
}