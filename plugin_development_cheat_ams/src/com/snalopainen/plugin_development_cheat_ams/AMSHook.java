package com.snalopainen.plugin_development_cheat_ams;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import android.content.ComponentName;
import android.content.Intent;

public class AMSHook implements InvocationHandler {

	private Object m_base;

	public AMSHook(Object base) {
		m_base = base;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// 拦截startActivity方法
		if ("startActivity".equals(method.getName())) {
			// 查找原始的intent对象
			
			int intentOfArgIndex = findFirstIntentIndexArgs(args);
			if (intentOfArgIndex >= 0) {
				Intent raw = null;
				raw = (Intent) args[intentOfArgIndex];
				// 看下是否是启动插件中的activity
				if (raw.getBooleanExtra(Constants.EXTRA_INVOKE_PLUGIN, false)) {
					// 获得原始的ComponentName
					ComponentName componentName = raw.getComponent();
					// 创建一个新的Intent
					Intent intent = new Intent();
					// 把Component替换为StubActivity的 这样就不会被系统检测到
					// 启动一个没有在AndroidManifest.xml//中声明的activity
					intent.setComponent(new ComponentName(componentName
							.getPackageName(), SubActivity.class
							.getCanonicalName()));
					// 保存原始的intent
					intent.putExtra(Constants.EXTRA_RAW_INTENT, raw);
					// 替换为新的Intent
					args[intentOfArgIndex] = intent;
				}
			}
		}
		// 还是按往常一样调用各种函数
		return method.invoke(m_base, args);
	}

	private static int findFirstIntentIndexArgs(Object[] args) {
		if (args != null && args.length > 0) {
			int i = 0;
			for (Object object : args) {
				if (object instanceof Intent) {
					return i;
				}
				i++;
			}
		}
		return -1;
	}

}