package com.faber.api.dm;

import com.faber.core.config.dbinit.DbInit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FaDocDbInit implements DbInit {

    @Override
    public String getNo() {
        return "fa-doc";
    }

    @Override
    public String getName() {
        return "业务模块名称";
    }

}
