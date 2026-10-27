package com.extra.service;

import java.util.List;

public interface ExtraService {
    List<ExtraVO> selectExtraList(ExtraFilterVO filterVO);
}
