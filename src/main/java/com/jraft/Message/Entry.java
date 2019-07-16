package com.jraft.Message;

import lombok.Data;

/**
 * @author chenchang
 * @date 2019/7/16 19:07
 */
@Data
public class Entry {
    private int term;
    private int index;
    private byte[] data;

}
