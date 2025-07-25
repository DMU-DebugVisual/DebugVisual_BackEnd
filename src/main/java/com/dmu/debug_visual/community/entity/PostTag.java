package com.dmu.debug_visual.community.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "게시글 태그", enumAsRef = true)
public enum PostTag {
    JAVA("Java"),
    C("C"),
    CPP("C++"),
    JPA("jpa"),
    JAVASCRIPT("JavaScript"),
    PYTHON("Python"),
    OOP("객체지향"),
    BIGDATA("빅데이터"),
    SPRING("spring"),
    TYPESCRIPT("TypeScript"),
    ML("머신러닝");

    private final String label;

    PostTag(String label) {
        this.label = label;
    }
}
