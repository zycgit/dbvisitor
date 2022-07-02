/*
 * Copyright 2008-2009 the original ������(zyc@hasor.net).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.jdbc.transaction;
/**
 * ���񴫲�����
 * @version : 2013-10-30
 * @author ������(zyc@hasor.net)
 */
public enum TransactionBehavior {
    /**
     * ������������
     * <p><i><b>����</b></i>�����Լ����Ѿ����ڵ������У����û������һ���µ�����*/
    PROPAGATION_REQUIRED,
    /**
     * ��������
     * <p><i><b>����</b></i>��������ǰ���ڵ��������������ڵĻ�����
     * ���ҿ���һ��ȫ�µ��������������Ѵ��ڵ�����֮��˴�û�й�ϵ��*/
    RROPAGATION_REQUIRES_NEW,
    /**
     * Ƕ������
     * <p><i><b>����</b></i>���ڵ�ǰ�����п���һ���������������ݽ�����ͬ��һ������һͬ�ݽ���
     * <p><i><b>ע��</b></i>����Ҫ����֧�ֱ���㡣*/
    PROPAGATION_NESTED,
    /**
     * ���滷��
     * <p><i><b>����</b></i>�������ǰû��������ڣ����Է�����ʽִ�У�����У���ʹ�õ�ǰ����*/
    PROPAGATION_SUPPORTS,
    /**
     * ������ʽ
     * <p><i><b>����</b></i>�������ǰû��������ڣ����Է�����ʽִ�У�����У��ͽ���ǰ�������
     * */
    PROPAGATION_NOT_SUPPORTED,
    /**
     * �ų�����
     * <p><i><b>����</b></i>�������ǰû��������ڣ����Է�����ʽִ�У�����У����׳��쳣��*/
    PROPAGATION_NEVER,
    /**
     * Ҫ�󻷾��д�������
     * <p><i><b>����</b></i>�������ǰû��������ڣ����׳��쳣������У���ʹ�õ�ǰ����*/
    PROPAGATION_MANDATORY,
}