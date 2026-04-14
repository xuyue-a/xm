package com.example.common;

/**
 * 统一返回包装类
 * 新增泛型支持，保留原有Object类型的兼容性
 */
public class Result<T> { // 新增泛型<T>
    private String code;
    private String msg;
    private T data; // 替换Object为泛型T（原有Object类型自动兼容）

    // ========== 原有无参构造器（保留，反射/序列化需要） ==========
    public Result() {
    }

    // ========== 原有静态方法（完全保留，确保现有代码无影响） ==========
    public static Result success() {
        Result result = new Result();
        result.setCode("200");
        result.setMsg("请求成功");
        return result;
    }

    public static Result success(Object data) {
        Result result = success();
        result.setData(data);
        return result;
    }

    public static Result error() {
        Result result = new Result();
        result.setCode("500");
        result.setMsg("系统错误");
        return result;
    }

    public static Result error(String code, String msg) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // 新增：只传错误信息的重载方法（原有，保留）
    public static Result error(String msg) {
        Result result = new Result();
        result.setCode("500"); // 默认500错误码
        result.setMsg(msg);
        return result;
    }

    // ========== 新增泛型静态方法（可选，用于强类型返回，不影响原有代码） ==========
    /**
     * 泛型成功返回（带数据，强类型）
     * @param data 强类型数据
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> successWithData(T data) {
        Result<T> result = new Result<>();
        result.setCode("200");
        result.setMsg("请求成功");
        result.setData(data);
        return result;
    }

    /**
     * 泛型错误返回（强类型，可选）
     * @param msg 错误信息
     * @param <T> 数据类型（通常为Void）
     * @return Result<T>
     */
    public static <T> Result<T> errorWithMsg(String msg) {
        Result<T> result = new Result<>();
        result.setCode("500");
        result.setMsg(msg);
        return result;
    }

    // ========== 原有getter/setter（完全保留） ==========
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    // 注意：这里的返回类型改为T，原有Object类型的调用会自动向上转型，无影响
    public T getData() {
        return data;
    }

    // 入参类型改为T，原有Object类型的传入会自动兼容（泛型擦除机制）
    public void setData(T data) {
        this.data = data;
    }
}