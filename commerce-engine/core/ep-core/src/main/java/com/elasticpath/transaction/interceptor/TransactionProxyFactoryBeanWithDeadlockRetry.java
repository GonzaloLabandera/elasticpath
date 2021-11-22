/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.transaction.interceptor;

import java.util.Properties;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

/**
 * Extension of TransactionProxyFactoryBean that uses the TransactionInterceptorWithDeadlockRetry.
 */
public class TransactionProxyFactoryBeanWithDeadlockRetry extends TransactionProxyFactoryBean {
	private static final long serialVersionUID = 1L;

	private final TransactionInterceptor transactionInterceptor = new TransactionInterceptorWithDeadlockRetry();

	/**
	 * Set the default transaction manager. This will perform actual
	 * transaction management: This class is just a way of invoking it.
	 * @see TransactionInterceptorWithDeadlockRetry#setTransactionManager
	 */
	@Override
	public void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionInterceptor.setTransactionManager(transactionManager);
	}

	/**
	 * Set properties with method names as keys and transaction attribute
	 * descriptors (parsed via TransactionAttributeEditor) as values:
	 * e.g. key = "myMethod", value = "PROPAGATION_REQUIRED,readOnly".
	 * <p>Note: Method names are always applied to the target class,
	 * no matter if defined in an interface or the class itself.
	 * <p>Internally, a NameMatchTransactionAttributeSource will be
	 * created from the given properties.
	 */
	@Override
	public void setTransactionAttributes(final Properties transactionAttributes) {
		this.transactionInterceptor.setTransactionAttributes(transactionAttributes);
	}

	/**
	 * Set the transaction attribute source which is used to find transaction
	 * attributes. If specifying a String property value, a PropertyEditor
	 * will create a MethodMapTransactionAttributeSource from the value.
	 */
	@Override
	public void setTransactionAttributeSource(final TransactionAttributeSource transactionAttributeSource) {
		this.transactionInterceptor.setTransactionAttributeSource(transactionAttributeSource);
	}

	/**
	 * This callback is optional: If running in a BeanFactory and no transaction
	 * manager has been set explicitly, a single matching bean of type
	 * {@link PlatformTransactionManager} will be fetched from the BeanFactory.
	 */
	@Override
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.transactionInterceptor.setBeanFactory(beanFactory);
	}

	/**
	 * Creates an advisor for this FactoryBean's TransactionInterceptor.
	 */
	@Override
	protected Object createMainInterceptor() {
		this.transactionInterceptor.afterPropertiesSet();
		return new TransactionAttributeSourceAdvisor(this.transactionInterceptor);
	}
}
