package com.wdbyte.network.icp.entity;

import lombok.Data;

@Data
public class IcpInfo {
    /**
     * 内容类型：如，新闻、文化等
     */
    private String contentTypeName;
    /**
     * 域名
     */
    private String domain;
    /**
     * 域名 ID
     */
    private long domainId;
    /**
     *
     */
    private String leaderName;
    /**
     * 限制访问：是/否
     */
    private String limitAccess;
    /**
     *
     */
    private long mainId;
    /**
     * 备案主题信息-备案号
     */
    private String mainLicence;
    /**
     * 主办单位性质：企业/个人
     */
    private String natureName;
    /**
     *
     */
    private long serviceId;
    /**
     * 备案网站信息-备案号
     */
    private String serviceLicence;
    /**
     * 主办单位名称：如 深圳市腾讯计算机系统有限公司
     */
    private String unitName;
    /**
     * 更新时间
     */
    private String updateRecordTime;


}
