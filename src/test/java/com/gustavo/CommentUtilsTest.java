package com.gustavo;

import com.gustavo.constant.BaseTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CommentUtilsTest {

    @Test
    void test2() {
        String dataType = "string";
        if (BaseTypeEnum.isName(dataType)) {
            var name = BaseTypeEnum.findByBoxName(dataType);
            Assertions.assertEquals("String", name);
        }
    }
}
