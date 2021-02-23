/**
 * Copyright (c) 2009. ElasticPath Software Inc. All rights reserved.
 */
package com.elasticpath.tags.builder

import com.elasticpath.tags.domain.Condition
import com.elasticpath.tags.domain.LogicalOperator

/**
 * Logical Operator Builder. This implementation builds a tree structure from 
 * condition string by using internal tree implementation.
 */
class LogicalTreeBuilder {
  private currentNodeList = new Stack<LogicalOperator>()
  private logicalOperator
  
  /**
   * Adds a condition leaf to the active logical operator tree.
   */
  def addCondition(key, keyString, Object params, functionName) {
	  if (params.getClass().isArray()) {
		  this.currentNodeList.peek().addCondition(new Condition(key, keyString, functionName, params[0]))
	  } else {
		  this.currentNodeList.peek().addCondition(new Condition(key, keyString, functionName, params))
	  }	  
  }
  
  /**
   * Adds a logical operator node to the active logical operator tree.
   */
  def addLogicalOperator(operatorType, params) {
	  def lo = new LogicalOperator(operatorType)

	  if (!this.currentNodeList.isEmpty()) {
		  lo.setParentLogicalOperator(this.currentNodeList.peek())
		  this.currentNodeList.peek().addLogicalOperator(lo)
	  }

	  this.currentNodeList.push(lo)
      params.each {
          it()
      }
	  this.logicalOperator = this.currentNodeList.pop()
  }
  
  /**
   * Returns the active logical operator tree.
   */
  def getLogicalOperator() {
	  return this.logicalOperator
  }
}
