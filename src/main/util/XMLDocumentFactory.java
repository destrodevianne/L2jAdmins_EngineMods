/*
 * L2J_EngineMods
 * Engine developed by Fissban.
 *
 * This software is not free and you do not have permission
 * to distribute without the permission of its owner.
 *
 * This software is distributed only under the rule
 * of www.devsadmins.com.
 * 
 * Contact us with any questions by the media
 * provided by our web or email marco.faccio@gmail.com
 */
package main.util;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class XMLDocumentFactory
{
	
	public static final XMLDocumentFactory getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final DocumentBuilder _builder;
	
	protected XMLDocumentFactory() throws Exception
	{
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			_builder = factory.newDocumentBuilder();
		}
		catch (Exception e)
		{
			throw new Exception("Failed initializing", e);
		}
	}
	
	public final Document loadDocument(final String filePath) throws Exception
	{
		return loadDocument(new File(filePath));
	}
	
	public final Document loadDocument(final File file) throws Exception
	{
		if (!file.exists() || !file.isFile())
		{
			throw new Exception("File: " + file.getAbsolutePath() + " doesn't exist and/or is not a file.");
		}
		
		return _builder.parse(file);
	}
	
	public final Document newDocument()
	{
		return _builder.newDocument();
	}
	
	private static class SingletonHolder
	{
		protected static final XMLDocumentFactory _instance;
		
		static
		{
			try
			{
				_instance = new XMLDocumentFactory();
			}
			catch (Exception e)
			{
				throw new ExceptionInInitializerError(e);
			}
		}
	}
	
}
