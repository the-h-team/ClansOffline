package com.github.sanctum.clansoffline.lib;

import org.jetbrains.annotations.NotNull;

public class ShieldManager extends Manager<Object>{

	private boolean ENABLED;

	public boolean isEnabled() {
		return ENABLED;
	}

	public void setEnabled(boolean enabled) {
		this.ENABLED = enabled;
	}

	@Override
	public boolean load(@NotNull Object o) {
		return false;
	}

	@Override
	public boolean remove(@NotNull Object o) {
		return false;
	}
}
