package org.strangeforest.tcb.stats.util;

import java.util.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

public interface CodedEnum {

	String getCode();
	String getText();

	static <E extends Enum<E> & CodedEnum> E decode(Class<E> enumClass, String code) {
		for (E e : enumClass.getEnumConstants()) {
			if (e.getCode().equals(code))
				return e;
		}
		throw new IllegalArgumentException(String.format("Invalid %1$s code: %2$s", enumClass.getSimpleName(), code));
	}

	static <E extends Enum<E> & CodedEnum> Map<String, String> asMap(Class<E> enumClass) {
		return Stream.of(enumClass.getEnumConstants()).collect(toMap(e -> e.getCode(), e -> e.getText()));
	}
}
