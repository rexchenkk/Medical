package com.medical.expert.data.key

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/12/08 2:48 PM
 * 年龄、性别、报告名称
 */
enum class CommonDataKey(val keyName: String) {
    SEX("性别"),
    AGE("年龄"),
//    REPORT_NAME("报告名称"),
//    REPORT_DETECT_NAME("报告单名称"),
    DIAGNOSIS("临床诊断"),
    DIAGNOSIS_METHOD("检查方法"),
    DIAGNOSIS_RESULT("检查所见"),
    DIAGNOSIS_TIPS("检查提示"),
    EYE_VISIBLE("肉眼可见"),
    SUGGESTION("建议"),
    DIAGNOSIS_PART("检查部位"),

}