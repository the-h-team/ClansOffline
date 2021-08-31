package com.github.sanctum.clansoffline.lib;

import org.jetbrains.annotations.NotNull;

public abstract class Manager<T> {

	public abstract boolean load(@NotNull T t);

	public abstract boolean remove(@NotNull T t);

}
