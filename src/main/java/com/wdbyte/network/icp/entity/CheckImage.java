package com.wdbyte.network.icp.entity;

import lombok.Data;

/**
 * 验证码信息
 *
 * @author https://www.wdbyte.com
 * @date 2023/07/07
 */
@Data
public class CheckImage {
    private String smallImage;
    private String bigImage;
    private String uuid;
}
