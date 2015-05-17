package at.yawk.catdb;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class Pair<K, V> {
    private K key;
    private V value;
}
