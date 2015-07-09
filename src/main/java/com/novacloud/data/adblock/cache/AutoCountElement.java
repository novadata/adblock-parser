package com.novacloud.data.adblock.cache;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("serial")
public class AutoCountElement<E> implements Serializable {
	private final E value;
	private final AtomicInteger count;
	public AutoCountElement(final E value) {
		this.value = value;
		this.count = new AtomicInteger(1);
	}
	public E getValue() {
		count.incrementAndGet();
		return value;
	}
	public int getCount() {
		return count.get();
	}
	public void divideCount(final double dividor) {
		count.getAndSet((int)(count.get()/dividor));
//		count.updateAndGet(new IntUnaryOperator()
//		{
//			@Override
//			public int applyAsInt(final int operand) {
//				return (int) (operand / dividor);
//			}
//		});
	}
}