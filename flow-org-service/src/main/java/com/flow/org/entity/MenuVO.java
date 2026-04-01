package com.flow.org.entity;

import lombok.Data;
import java.util.List;

@Data
public class MenuVO {
    private Long id;
    private String name;
    private String path;
    private String component;
    private String icon;
    private String type;
    private Long parentId;
    private Integer sort;
    private String method;
    private List<MenuVO> children;
}
