/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.common.dto.store;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.elasticpath.common.dto.Dto;

/**
 * The implementation of the <code>Dto</code> interface that contains data of CartType object.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = CartTypeDTO.ROOT_ELEMENT)
public class CartTypeDTO implements Dto {

	private static final long serialVersionUID = 1L;
	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "carttype";

	@XmlElement(name = "name", required = true)
	private String name;

	@XmlElement(name = "guid", required = true)
	private String guid;

	@XmlElementWrapper(name = "modifiergroups")
	@XmlElement(name = "code")
	private List<String> modifierGroups;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<String> getModifierGroups() {
		return modifierGroups;
	}

	public void setModifierGroups(final List<String> modifierGroups) {
		this.modifierGroups = modifierGroups;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(final String guid) {
		this.guid = guid;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("name", getName())
				.append("guid", getGuid())
				.append("modifierGroups", getModifierGroups().toString())
				.toString();
	}

}
