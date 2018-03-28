package com.cairone.olingo.ext.jpa.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cairone.olingo.ext.jpa.annotations.EdmComplex;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.interfaces.OdataEnum;

public class BaseProcessorTests {

	public static final Logger LOG = LoggerFactory.getLogger(BaseProcessorTests.class);
	
	@Test
	public void writeEntityTest() {
		
		TargetEdm targetEdm = new TargetEdm(
				FIELD_ID, 
				FIELD_COUNTER, 
				FIELD_NAME, 
				FIELD_ANY_DATE, 
				FIELD_EXACTLY_WHEN, 
				FIELD_IS_OK, 
				FIELD_TARGET_ENUM,
				new ComplexClass(CPX_FIELD_ID, CPX_FIELD_NAME),
				FIELD_LST_CONTACTS,
				FIELD_LST_NUMS);
		BaseProcessor baseProcessor = new BaseProcessor();
		
		try {
			Entity entity = baseProcessor.writeEntity(targetEdm);
			assertNotNull(entity);
			
			LOG.info("Entity: {}", entity.getId());
			LOG.info("Properties count: {}", entity.getProperties().size());
			
			assertEquals("PROPERTIES COUNT NOT MATCH", PROPERTIES_COUNT, entity.getProperties().size());
			
			
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException | ODataApplicationException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	public static final Long FIELD_ID = 1L;
	public static final Integer FIELD_COUNTER = 1;
	public static final String FIELD_NAME = "SOMEONE";
	public static final LocalDate FIELD_ANY_DATE = LocalDate.now();
	public static final LocalDateTime FIELD_EXACTLY_WHEN = LocalDateTime.now();
	public static final Boolean FIELD_IS_OK = Boolean.TRUE;
	public static final TargetEnum FIELD_TARGET_ENUM = TargetEnum.ONE;
	public static final Integer CPX_FIELD_ID = 1;
	public static final String CPX_FIELD_NAME = "COMPLEX";
	public static final List<String> FIELD_LST_CONTACTS = Arrays.asList("C1", "C2", "C3", "C4");
	public static final ArrayList<Integer> FIELD_LST_NUMS = new ArrayList<Integer>( Arrays.asList(1, 2, 3) );
	
	public static final int PROPERTIES_COUNT = 10;

	@EdmEntitySet("Targets")
	@EdmEntity(name="Target", namespace="com.testing", key="Id")
	class TargetEdm extends BaseTargetEdm {
		
		@EdmProperty(nullable=false)
		private Long id = null;
		
		@EdmProperty(nullable=false)
		private String name = null;
		
		@EdmProperty
		private LocalDate anyDate = null;
		
		@EdmProperty
		private Boolean isOk = null;
		
		@EdmProperty
		private TargetEnum targetEnum = null;
		
		@EdmProperty
		private ComplexClass complex = null;
		
		@EdmProperty(nullable=false)
		private List<String> contacts = null;

		@EdmProperty(nullable=false)
		private ArrayList<Integer> magicNumbers = null;
		
		@EdmNavigationProperty
		private CityEdm city = null;
		
		public TargetEdm() {}

		public TargetEdm(Long id, Integer counter, String name, LocalDate anyDate, LocalDateTime exactlyWhen, Boolean isOk, TargetEnum targetEnum, ComplexClass complex, List<String> contacts, ArrayList<Integer> magicNumbers) {
			super(counter, exactlyWhen);
			this.id = id;
			this.name = name;
			this.anyDate = anyDate;
			this.isOk = isOk;
			this.targetEnum = targetEnum;
			this.complex = complex;
			this.contacts = contacts;
			this.magicNumbers = magicNumbers;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public LocalDate getAnyDate() {
			return anyDate;
		}

		public void setAnyDate(LocalDate anyDate) {
			this.anyDate = anyDate;
		}

		public Boolean getIsOk() {
			return isOk;
		}

		public void setIsOk(Boolean isOk) {
			this.isOk = isOk;
		}

		public TargetEnum getTargetEnum() {
			return targetEnum;
		}

		public void setTargetEnum(TargetEnum targetEnum) {
			this.targetEnum = targetEnum;
		}

		public ComplexClass getComplex() {
			return complex;
		}

		public void setComplex(ComplexClass complex) {
			this.complex = complex;
		}

		public List<String> getContacts() {
			return contacts;
		}

		public void setContacts(List<String> contacts) {
			this.contacts = contacts;
		}

		public ArrayList<Integer> getMagicNumbers() {
			return magicNumbers;
		}

		public void setMagicNumbers(ArrayList<Integer> magicNumbers) {
			this.magicNumbers = magicNumbers;
		}
		
	}
	
	class BaseTargetEdm {

		@EdmProperty
		private Integer counter = null;

		@EdmProperty
		private LocalDateTime exactlyWhen = null;
		
		public BaseTargetEdm() {}

		public BaseTargetEdm(Integer counter, LocalDateTime exactlyWhen) {
			super();
			this.counter = counter;
			this.exactlyWhen = exactlyWhen;
		}

		public Integer getCounter() {
			return counter;
		}

		public void setCounter(Integer counter) {
			this.counter = counter;
		}

		public LocalDateTime getExactlyWhen() {
			return exactlyWhen;
		}

		public void setExactlyWhen(LocalDateTime exactlyWhen) {
			this.exactlyWhen = exactlyWhen;
		}
	}
	
	@EdmEntitySet("Cities")
	@EdmEntity(name="City", namespace="com.testing", key="Id")
	class CityEdm {
		@EdmProperty private Long id = null;
		@EdmProperty private String name = null;
		@EdmProperty private BigDecimal gdp = null;
		public CityEdm() {}
		public CityEdm(Long id, String name, BigDecimal gdp) {
			super();
			this.id = id;
			this.name = name;
			this.gdp = gdp;
		}
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public BigDecimal getGdp() {
			return gdp;
		}
		public void setGdp(BigDecimal gdp) {
			this.gdp = gdp;
		}
	}
	
	@EdmComplex(name="Complex", namespace="com.testing")
	class ComplexClass {
		@EdmProperty private Integer id = null;
		@EdmProperty private String name = null;
		public ComplexClass() {}
		public ComplexClass(Integer id, String name) {
			super();
			this.id = id;
			this.name = name;
		}
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	
	public enum TargetEnum implements OdataEnum<TargetEnum> {
		ONE, TWO, THREE;
		private int ordinal;
		@Override
		public int getOrdinal() {
			return ordinal;
		}

		@Override
		public TargetEnum setOrdinal(int ordinal) {
			switch(ordinal) {
			case 2:
				return THREE;
			case 1:
				return TWO;
			default:
				return ONE;
			}
		}
	}
}
