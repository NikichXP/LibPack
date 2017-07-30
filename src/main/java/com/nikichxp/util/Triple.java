package com.nikichxp.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Triple <A, B, C> {
	
	private A key;
	private B dual;
	private C value;
	
}
