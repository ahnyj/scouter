/*
 *  Copyright 2015 LG CNS.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */

package scouter.agent.asm;

import java.util.List;

import scouter.agent.ClassDesc;
import scouter.agent.Configure;
import scouter.agent.asm.util.AsmUtil;
import scouter.agent.asm.util.MethodSet;
import scouter.agent.trace.TraceMain;
import scouter.org.objectweb.asm.ClassVisitor;
import scouter.org.objectweb.asm.MethodVisitor;
import scouter.org.objectweb.asm.Opcodes;
import scouter.org.objectweb.asm.Type;
import scouter.org.objectweb.asm.commons.LocalVariablesSorter;

public class CapArgsASM implements IASM, Opcodes {
	private List<MethodSet> target = MethodSet.getHookingMethodSet(Configure.getInstance().hook_args);

	public boolean isTarget(String className) {
		for (int i = 0; i < target.size(); i++) {
			MethodSet mset = target.get(i);
			if (mset.classMatch.include(className)) {
				return true;
			}
		}
		return false;
	}
	public ClassVisitor transform(ClassVisitor cv, String className,
			ClassDesc classDesc) {

		for (int i = 0; i < target.size(); i++) {
			MethodSet mset = target.get(i);
			if (mset.classMatch.include(className)) {
				return new CapArgsCV(cv, mset, className);
			}
		}
		return cv;
	}
}

// ///////////////////////////////////////////////////////////////////////////
class CapArgsCV extends ClassVisitor implements Opcodes {

	public String className;
	private MethodSet mset;

	public CapArgsCV(ClassVisitor cv, MethodSet mset, String className) {
		super(ASM4, cv);
		this.mset = mset;
		this.className = className;
	}

	@Override
	public MethodVisitor visitMethod(int access, String methodName,
			String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, methodName, desc,
				signature, exceptions);
		if (mv == null || mset.isA(methodName, desc) == false) {
			return mv;
		}
		if (AsmUtil.isSpecial(methodName)) {
			return mv;
		}
		// String fullname = AsmUtil.add(className, name, desc);
		// int fullname_hash = HashUtil.hash(fullname);
		return new CapArgsMV(access, desc, mv, Type.getArgumentTypes(desc),
				(access & ACC_STATIC) != 0, className, methodName, desc);
	}
}

// ///////////////////////////////////////////////////////////////////////////
class CapArgsMV extends LocalVariablesSorter implements Opcodes {
	private static final String CLASS = TraceMain.class.getName().replace('.',
			'/');
	private static final String METHOD = "capArgs";
	private static final String SIGNATURE = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V";

	private Type[] paramTypes;
	private boolean isStatic;
	private String className;
	private String methodName;
	private String methodDesc;

	public CapArgsMV(int access, String desc, MethodVisitor mv,
			Type[] paramTypes, boolean isStatic, String classname,
			String methodname, String methoddesc) {
		super(ASM4,access, desc, mv);
		this.paramTypes = paramTypes;
		this.isStatic = isStatic;
		this.className = classname;
		this.methodName = methodname;
		this.methodDesc = methoddesc;

	}

	@Override
	public void visitCode() {

		int sidx = isStatic ? 0 : 1;

		int arrVarIdx = newLocal(Type.getType("[Ljava/lang/Object;"));
		AsmUtil.PUSH(mv, paramTypes.length);
		mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
		mv.visitVarInsn(Opcodes.ASTORE, arrVarIdx);

		// if (isStatic == false) {
		// mv.visitVarInsn(Opcodes.ALOAD, arrVarIdx);
		// AsmUtil.PUSH(mv, 0);
		// mv.visitVarInsn(Opcodes.ALOAD, 0);
		// mv.visitInsn(Opcodes.AASTORE);
		// }

		for (int i = 0; i < paramTypes.length; i++) {
			Type tp = paramTypes[i];
			mv.visitVarInsn(Opcodes.ALOAD, arrVarIdx);
			AsmUtil.PUSH(mv, i);

			switch (tp.getSort()) {
			case Type.BOOLEAN:
				mv.visitVarInsn(Opcodes.ILOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean",
						"valueOf", "(Z)Ljava/lang/Boolean;");
				break;
			case Type.BYTE:
				mv.visitVarInsn(Opcodes.ILOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte",
						"valueOf", "(B)Ljava/lang/Byte;");
				break;
			case Type.CHAR:
				mv.visitVarInsn(Opcodes.ILOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character",
						"valueOf", "(C)Ljava/lang/Character;");
				break;
			case Type.SHORT:
				mv.visitVarInsn(Opcodes.ILOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short",
						"valueOf", "(S)Ljava/lang/Short;");
				break;
			case Type.INT:
				mv.visitVarInsn(Opcodes.ILOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer",
						"valueOf", "(I)Ljava/lang/Integer;");
				break;
			case Type.LONG:
				mv.visitVarInsn(Opcodes.LLOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long",
						"valueOf", "(J)Ljava/lang/Long;");
				break;
			case Type.FLOAT:
				mv.visitVarInsn(Opcodes.FLOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float",
						"valueOf", "(F)Ljava/lang/Float;");
				break;
			case Type.DOUBLE:
				mv.visitVarInsn(Opcodes.DLOAD, sidx);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double",
						"valueOf", "(D)Ljava/lang/Double;");
				break;
			default:
				mv.visitVarInsn(Opcodes.ALOAD, sidx);
			}
			mv.visitInsn(Opcodes.AASTORE);
			sidx += tp.getSize();
		}
		AsmUtil.PUSH(mv, className);
		AsmUtil.PUSH(mv, methodName);
		AsmUtil.PUSH(mv, methodDesc);
		mv.visitVarInsn(Opcodes.ALOAD, arrVarIdx);

		mv.visitMethodInsn(Opcodes.INVOKESTATIC, CLASS, METHOD, SIGNATURE);
		super.visitCode();
	}
}