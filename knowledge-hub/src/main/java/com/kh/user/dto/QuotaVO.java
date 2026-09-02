package com.kh.user.dto;

/**
 * 存储配额视图：quota 上限(字节) / used 已用(字节) / percentage 使用百分比(1位小数)。
 * 上传前的配额校验与前端进度条共用此口径；秒传亦计入 used（文件对用户逻辑可见即算占用）。
 */
public record QuotaVO(
        long quota,
        long used,
        double percentage
) {
}
