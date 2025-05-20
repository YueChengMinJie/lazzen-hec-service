package com.lazzen.hec.form;

import lombok.Data;

@Data
public class PageForm {
    private int page = 1;

    private int size = 10;
}
