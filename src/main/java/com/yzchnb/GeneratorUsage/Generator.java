package com.yzchnb.GeneratorUsage;

import com.yzchnb.entity.FunctionDetail;

public interface Generator {
    String generate(FunctionDetail detail) throws Exception;
}
