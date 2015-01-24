package org.bitcoin.authenticator;

import java.util.List;

public class TestUtil {
	public static <T> boolean listEquals(List<T> list1, List<T> list2) {
        if(list1.size() != list2.size())
            return true;
        for (T t : list1) {
            if(!list2.contains(t))
                return false;
        }
        return true;
    }
}
