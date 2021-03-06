package com.redhat.syseng.openshift.service.persistent.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class ProvisionRecord
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sku;
	private String name;
	private String description;
	private Double length;
	private Double width;
	private Double height;
	private Double weight;
	private Boolean featured;
	private Integer availability;
	private BigDecimal price;
	private String image;


	public Long getSku()
	{
		return sku;
	}

	public void setSku(Long sku)
	{
		this.sku = sku;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Double getLength()
	{
		return length;
	}

	public void setLength(Double length)
	{
		this.length = length;
	}

	public Double getWidth()
	{
		return width;
	}

	public void setWidth(Double width)
	{
		this.width = width;
	}

	public Double getHeight()
	{
		return height;
	}

	public void setHeight(Double height)
	{
		this.height = height;
	}

	public Double getWeight()
	{
		return weight;
	}

	public void setWeight(Double weight)
	{
		this.weight = weight;
	}

	public Boolean getFeatured()
	{
		return featured;
	}

	public void setFeatured(Boolean featured)
	{
		this.featured = featured;
	}

	public Integer getAvailability()
	{
		return availability;
	}

	public void setAvailability(Integer availability)
	{
		this.availability = availability;
	}

	public BigDecimal getPrice()
	{
		return price;
	}

	public void setPrice(BigDecimal price)
	{
		this.price = price;
	}

	public String getImage()
	{
		return image;
	}

	public void setImage(String image)
	{
		this.image = image;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( sku == null ) ? 0 : sku.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		ProvisionRecord other = (ProvisionRecord)obj;
		if( sku == null )
		{
			if( other.sku != null )
				return false;
		}
		else if( !sku.equals( other.sku ) )
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Product [sku=" + sku + ", name=" + name + ", description=" + description + ", length=" + length + ", width=" + width + ", height="
				+ height + ", weight=" + weight + ", featured=" + featured + ", availability=" + availability + ", price=" + price + ", image="
				+ image + "]";
	}
}