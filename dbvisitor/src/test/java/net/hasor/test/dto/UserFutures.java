/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.dto;
import java.util.Date;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class UserFutures {
    private String  ext1;
    private Integer ext2;
    private Boolean ext3;
    private Date    ext4;

    public String getExt1() {
        return ext1;
    }

    public void setExt1(String ext1) {
        this.ext1 = ext1;
    }

    public Integer getExt2() {
        return ext2;
    }

    public void setExt2(Integer ext2) {
        this.ext2 = ext2;
    }

    public Boolean getExt3() {
        return ext3;
    }

    public void setExt3(Boolean ext3) {
        this.ext3 = ext3;
    }

    public Date getExt4() {
        return ext4;
    }

    public void setExt4(Date ext4) {
        this.ext4 = ext4;
    }
}
