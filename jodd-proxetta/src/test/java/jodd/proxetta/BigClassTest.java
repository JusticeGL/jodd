// Copyright (c) 2003-present, Jodd Team (http://jodd.org)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package jodd.proxetta;

import jodd.asm7.Type;
import jodd.introspector.ClassDescriptor;
import jodd.introspector.ClassIntrospector;
import jodd.mutable.MutableBoolean;
import jodd.proxetta.fixtures.data.Action;
import jodd.proxetta.fixtures.data.BigFatJoe;
import jodd.proxetta.fixtures.data.InterceptedBy;
import jodd.proxetta.fixtures.data.MadvocAction;
import jodd.proxetta.fixtures.data.PetiteBean;
import jodd.proxetta.fixtures.data.PetiteInject;
import jodd.proxetta.fixtures.data.StatCounter;
import jodd.proxetta.fixtures.data.StatCounterAdvice;
import jodd.proxetta.fixtures.data.Transaction;
import jodd.util.DefineClass;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BigClassTest {

	@Test
	void testAllFeatures() throws IOException, IllegalAccessException, InstantiationException {
		StatCounter.counter = 0;
		final MutableBoolean firstTime = new MutableBoolean(true);

		final ProxyAspect aspect = new ProxyAspect(StatCounterAdvice.class, mi -> {
			if (firstTime.value) {
				firstTime.value = false;
				final ClassInfo ci = mi.getClassInfo();
				assertEquals("BigFatJoe", ci.getClassname());
				assertEquals(BigFatJoe.class.getPackage().getName(), ci.getPackage());
				assertEquals("jodd/proxetta/fixtures/data/BigFatJoe", ci.getReference());
				assertEquals("jodd/proxetta/fixtures/data/SmallSkinnyZoe", ci.getSuperName());
				final AnnotationInfo[] anns = ci.getAnnotations();
				assertNotNull(anns);
				assertEquals(3, anns.length);
				AnnotationInfo ai = anns[0];
				assertSame(ai, ci.getAnnotation(MadvocAction.class));
				assertEquals(MadvocAction.class.getName(), ai.getAnnotationClassname());
				assertEquals("L" + MadvocAction.class.getName().replace('.', '/') + ";", ai.getAnnotationSignature());
				assertEquals("madvocAction", ai.getElement("value"));
				ai = anns[1];
				assertSame(ai, ci.getAnnotation(PetiteBean.class));
				assertEquals(PetiteBean.class.getName(), ai.getAnnotationClassname());
				assertEquals("L" + PetiteBean.class.getName().replace('.', '/') + ";", ai.getAnnotationSignature());
				ai = anns[2];
				assertSame(ai, ci.getAnnotation(InterceptedBy.class));
				assertEquals(InterceptedBy.class.getName(), ai.getAnnotationClassname());
				assertEquals("L" + InterceptedBy.class.getName().replace('.', '/') + ";", ai.getAnnotationSignature());
				assertTrue(ai.getElement("value") instanceof Object[]);
				assertFalse(ai.getElement("value") instanceof String[]);
				final Object c1 = ((Object[]) ai.getElement("value"))[0];
				assertEquals("Ljodd/proxetta/fixtures/data/Str;", ((Type) c1).getDescriptor());
			}
			if (mi.getMethodName().equals("publicMethod")) {
				final AnnotationInfo[] anns = mi.getAnnotations();
				assertNotNull(anns);
				assertEquals(3, anns.length);

				AnnotationInfo ai = anns[0];
				assertSame(ai, mi.getAnnotation(Action.class));
				assertEquals(Action.class.getName(), ai.getAnnotationClassname());
				assertEquals("value", ai.getElement("value"));
				assertEquals("alias", ai.getElement("alias"));

				ai = anns[1];
				assertSame(ai, mi.getAnnotation(PetiteInject.class));
				assertEquals(PetiteInject.class.getName(), ai.getAnnotationClassname());
				assertEquals(0, ai.getElementNames().size());

				ai = anns[2];
				assertSame(ai, mi.getAnnotation(Transaction.class));
				assertEquals(Transaction.class.getName(), ai.getAnnotationClassname());
				assertEquals(2, ai.getElementNames().size());
				final String s = (String) ai.getElement("propagation");
				assertEquals("PROPAGATION_REQUIRES_NEW", s);
			}
			if (mi.getMethodName().equals("superPublicMethod")) {
				final AnnotationInfo[] anns = mi.getAnnotations();
				assertNotNull(anns);
				assertEquals(3, anns.length);

				AnnotationInfo ai = anns[0];
				assertSame(ai, mi.getAnnotation(Action.class));
				assertEquals(Action.class.getName(), ai.getAnnotationClassname());
				assertEquals(0, ai.getElementNames().size());

				ai = anns[1];
				assertSame(ai, mi.getAnnotation(PetiteInject.class));
				assertEquals(PetiteInject.class.getName(), ai.getAnnotationClassname());
				assertEquals(0, ai.getElementNames().size());

				ai = anns[2];
				assertSame(ai, mi.getAnnotation(Transaction.class));
				assertEquals(Transaction.class.getName(), ai.getAnnotationClassname());
				assertEquals(0, ai.getElementNames().size());
			}
			//System.out.println(!isRootMethod(mi) + " " + mi.getDeclaredClassName() + '#' + mi.getMethodName());
			return !mi.isRootMethod();
		});

		final byte[] classBytes = Proxetta.proxyProxetta().withAspect(aspect).proxy().setTarget(BigFatJoe.class).create();
//		URL resource = BigFatJoe.class.getResource("/" + BigFatJoe.class.getName().replace(".", "/") + ".class");
//		jodd.io.FileUtil.copy(FileUtil.toFile(resource), new java.io.File(SystemUtil.getUserHome(), "jo.class"));
//		jodd.io.FileUtil.writeBytes(new java.io.File(SystemUtil.getUserHome(), "joe.class"), classBytes);
		final Class clazz = DefineClass.of(null, classBytes, null);
		final BigFatJoe bigFatJoe = (BigFatJoe) clazz.newInstance();

		assertEquals(BigFatJoe.class.getName() + ProxettaNames.proxyClassNameSuffix, bigFatJoe.getClass().getName());
		assertEquals(BigFatJoe.class, ProxettaUtil.resolveTargetClass(bigFatJoe.getClass()));

		// test invocation

		assertEquals(3, StatCounter.counter);        // 2 x static + 1 x instance
		bigFatJoe.publicMethod();
		assertEquals(4, StatCounter.counter);
		bigFatJoe.callInnerMethods();
		assertEquals(7, StatCounter.counter);        // private method is not overridden

		bigFatJoe.superPublicMethod();
		assertEquals(8, StatCounter.counter);
		bigFatJoe.callInnerMethods2();
		assertEquals(9, StatCounter.counter);        // only public super methods are overridden

		// test class annotation
		final MadvocAction ma = (MadvocAction) clazz.getAnnotation(MadvocAction.class);
		assertEquals("madvocAction", ma.value());

		final InterceptedBy ib = (InterceptedBy) clazz.getAnnotation(InterceptedBy.class);
		assertNotNull(ib.value());
		assertEquals(2, ib.value().length);


		// test method annotation
		final ClassDescriptor cd = ClassIntrospector.get().lookup(clazz);
		final Method m = cd.getMethodDescriptor("publicMethod", false).getMethod();
		assertNotNull(m);
		final Annotation[] aa = m.getAnnotations();
		assertEquals(3, aa.length);
		final Action a = (Action) aa[0];
		assertEquals("alias", a.alias());
		assertEquals("extension", a.extension());
		assertEquals("method", a.method());
		assertEquals("value", a.value());

		final PetiteInject pi = (PetiteInject) aa[1];
		assertEquals("", pi.value());

		final Transaction tx = (Transaction) aa[2];
		assertTrue(tx.readOnly());
		assertEquals(1000, tx.timeout());
		assertEquals("PROPAGATION_REQUIRES_NEW", tx.propagation());

		bigFatJoe.runInnerClass();
		assertEquals(11, StatCounter.counter);        // proxy + call

	}
}
