package com.dma.svc;

import java.io.File;
import java.io.StringReader;
import java.util.Map;
import java.util.logging.Level;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.cognos.developer.schemas.bibus._3.AddOptions;
import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.Folder;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import com.cognos.developer.schemas.bibus._3.Sort;
import com.cognos.developer.schemas.bibus._3.TokenProp;
import com.cognos.developer.schemas.bibus._3.UpdateActionEnum;
import com.dma.cognos.CRNConnect;
import com.dma.web.Field;
import com.dma.web.QuerySubject;
import com.dma.web.RelationShip;

import sapphire.util.Logger;

public class FactorySVC {

	CognosSVC csvc;
	// Path contextRoot
	public FactorySVC (CognosSVC csvc)
	{
		this.csvc = csvc;
	}
	
	public void addLocale(String locale, String cognosDefaultLocale) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/addLocale.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node newLocale = document.selectSingleNode("//param[@seq='1']/value");
			Node activeLocale = document.selectSingleNode("//param[@seq='2']/value");

			newLocale.setText("<stringCollection><item>" + locale + "</item></stringCollection>");
			activeLocale.setText(cognosDefaultLocale);
			System.out.println("addLocale(" + locale + ")");
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}

	}

	public void removeLocale(String locale) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/removeLocale.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node nodeLocale = document.selectSingleNode("//param[@seq='1']/value");

			nodeLocale.setText(locale);

			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}

	}

	public void setLocale(String cognosDefaultLocale) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/setLocale.xml");

			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Element root = document.getRootElement();

			Node n1 = document.selectSingleNode("//action[@type='SetActiveLocale']/inputparams/param/value");
			Node n2 = document.selectSingleNode("//action[@type='SetDefaultLocale']/inputparams/param/value");

			n1.setText(cognosDefaultLocale);
			n2.setText(cognosDefaultLocale);
			
			System.out.println("setLocale " + "ActiveLocale=" + cognosDefaultLocale + " DefaultLocale=" + cognosDefaultLocale);
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}

	}
	
	public void changePropertyFixIDDefaultLocale() {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/changePropertyFixIDDefaultLocale.xml");

			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}

	}

	public void copyQuerySubject(String targetNameSpace, String sourceQS){
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/copyQuerySubject.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			
			Node n1 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150410184900010\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			n1.setText(targetNameSpace);

			Node n2 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150410184900010\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			n2.setText(sourceQS);

			System.out.println("copyQuerySubject(" + targetNameSpace + ", " + sourceQS + ")");
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
		
	}
	
	public void renameQuerySubject(String qs, String name){
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/renameQuerySubject.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			
			Node n1 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150323183114850\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			n1.setText("/O/name[0]/O/" + qs);

			Node n2 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150323183114850\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			n2.setText(name);

			System.out.println("renameQuerySubject(" + qs + ", " + name + ")");
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
		
	}
	
	public void DBImport(String Namespace, String dataSourceName, String catalogName, String schemaName, String engineName) {
		try {
			File xmlFile = null;
			if (engineName.equals("ORA")) {
				xmlFile = new File(csvc.getPathToXML() + "/DBImport_ORA.xml");
			} else  if (engineName.equals("MYSQL")) {
				xmlFile = new File(csvc.getPathToXML() + "/DBImport_MYSQL.xml");
			} else  if (engineName.equals("SQLSRV")) {
				xmlFile = new File(csvc.getPathToXML() + "/DBImport_SQLSRV.xml");
			}

			SAXReader reader = new SAXReader();
			Document script = reader.read(xmlFile);

			Element root = script.getRootElement();
			Node namespace = script.selectSingleNode("//param[@seq='1']/value");

			namespace.setText("[" + Namespace + "]");
			
			Element cdata = (Element) script.selectSingleNode("//param[@seq='2']/value");
			String s = cdata.getText();

			Document doc = reader.read(new StringReader(s));
			
			Element elemDataSource = (Element) doc.selectSingleNode("//item");
			elemDataSource.addAttribute("Name", dataSourceName);
			elemDataSource.addAttribute("description", "");
			elemDataSource.addAttribute("screenTip", "");

			if (engineName.equals("ORA")) {
				Element elemSchema = (Element) doc.selectSingleNode("//item/item");
				elemSchema.addAttribute("Name", schemaName);
			} else  if (engineName.equals("MYSQL")) {
				Element elemSchema = (Element) doc.selectSingleNode("//item/item");
				elemSchema.addAttribute("Name", schemaName);
			} else  if (engineName.equals("SQLSRV")) {
				Element elemCatalog = (Element) doc.selectSingleNode("//item/item");
				Element elemSchema = (Element) doc.selectSingleNode("//item/item/item");
				elemCatalog.addAttribute("Name", catalogName);
				elemSchema.addAttribute("Name", schemaName);
			}
			
			Element root_cdata = doc.getRootElement();
	
			// attach cdata
		    cdata.setText("");
		    cdata.addCDATA(root_cdata.asXML());
			
		    System.out.println(script.asXML());
		    System.out.println("DBImport(" + Namespace + ", " + dataSourceName + ", " + schemaName + ")");
			csvc.executeModel(script);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void deleteDataSource(String datasource) {
		try {
//			String datasource = "[].[dataSources].[DEV]";
			
			File xmlFile = new File(csvc.getPathToXML() + "/deleteDataSource.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20180206111718781\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");

			handle.setText(datasource);
	
//			System.out.println(document.asXML());
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	
	public void createFolder(String NamespaceRefObj, String Name) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createFolder.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node n1 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150727115905548\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			n1.setText(NamespaceRefObj);

			Node n2 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150727115905548\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[3]/value");
			n2.setText(Name);

			System.out.println("createFolder: Namespace " + NamespaceRefObj + ", Name " + Name);
//			System.out.println(document.asXML());
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void createFolderInFolder(String Namespace, String Fin, String Fout) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createFolderInFolder.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node n1 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150916123602448\"]/transaction[@saved=\"false\"]/action[1]/inputparams/param[2]/value");
			n1.setText(Namespace + ".[" + Fin + "]");

			Node n2 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150916123602448\"]/transaction[@saved=\"false\"]/action[1]/inputparams/param[3]/value");
			n2.setText(Fout);

			Node n3 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150916123602448\"]/transaction[@saved=\"false\"]/action[2]/inputparams/param[@seq=\"1\"]/value");
			n3.setText(Namespace + ".[" + Fout + "]");

			System.out.println("createFolderInFolder: Namespace " + Namespace + ", Fin " + Fin + ", Fout " + Fout);
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void createNamespace(String NameSpace, String Parent) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createNamespace.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node parent = document.selectSingleNode("//param[@seq='2']/value");
			Node namespace = document.selectSingleNode("//param[@seq='3']/value");

			parent.setText("[" + Parent + "]");
			namespace.setText(NameSpace);

			System.out.println("createNamespace(" + NameSpace + ", " + Parent + ")") ;
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	
	public void deleteNamespace(String namespace) {
		try {
	
			File xmlFile = new File(csvc.getPathToXML() + "/deleteNamespace.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171117155621486\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");

			handle.setText(namespace);
			// System.out.println(document.asXML());
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void createQuerySubject(String Source, String Destination, String Name) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createQuerySubject.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node src = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[1]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node dst = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[2]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node mov = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[2]/action[@seq=\"1\"]/inputparams/param[2]/value");
			Node mod_path = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[3]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node mod_val = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[3]/action[@seq=\"1\"]/inputparams/param[2]/value");

			src.setText("[" + Source + "].[" + Name + "]");
			dst.setText("[" + Destination + "]");
			mov.setText("[" + Source + "].[" + Name + "_" + Name + "]");
			mod_path.setText("/O/name[0]/O/[" + Destination + "].[" + Name + "_" + Name + "]");
			mod_val.setText(Name);

			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void createQuerySubjectInFolder(String Source, String Destination, String Folder, String Name) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createQuerySubject.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			///////////////////////////////////////////////////

			//////////////////////////////////////////////////
			Node src = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[1]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node dst = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[2]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node dstType = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[2]/action[@seq=\"1\"]/inputparams/param[1]/mappingpath");
			Node mov = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[2]/action[@seq=\"1\"]/inputparams/param[2]/value");
			Node mod_path = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[3]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node mod_val = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[3]/action[@seq=\"1\"]/inputparams/param[2]/value");

			src.setText("[" + Source + "].[" + Name + "]");
			dst.setText("[" + Destination + "].[" + Folder + "]");
			dstType.setText("folder");
			mov.setText("[" + Source + "].[" + Name + "_" + Name + "]");
			mod_path.setText("/O/name[0]/O/[" + Destination + "].[" + Name + "_" + Name + "]");
			mod_val.setText(Name);

			System.out.println("+-+-+-+-+-+-+- CREATE QS " + Source + ", " + Destination + ", " + Folder + ", " + Name);
			
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	// ceci pour les QS qui pointe directement sur la DB.
	// il ont besoin du vrai nom de la table et aussi du u_name
	public void createQuerySubject(String Source, String Destination, String BasedOnQuerySubject, String NameQuerySubject) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createQuerySubject.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node src = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[1]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node dst = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[2]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node mov = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[2]/action[@seq=\"1\"]/inputparams/param[2]/value");
			Node mod_path = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[3]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node mod_val = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150722173540031\"]/transaction[3]/action[@seq=\"1\"]/inputparams/param[2]/value");

			src.setText("[" + Source + "].[" + BasedOnQuerySubject + "]");
			dst.setText("[" + Destination + "]");

			mov.setText("[" + Source + "].[" + BasedOnQuerySubject + "_" + BasedOnQuerySubject + "]");
			mod_path.setText("/O/name[0]/O/[" + Destination + "].[" + BasedOnQuerySubject + "_" + BasedOnQuerySubject + "]");
			mod_val.setText(NameQuerySubject);
			
//			System.out.println(document.asXML());
			System.out.println("createQuerySubject(" + Source + ", " + Destination + ", " + BasedOnQuerySubject + ", " + BasedOnQuerySubject + ")");
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	
	public void createQuerySubjectFilter(String qsPath, String Exp) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createQuerySubjectFilter.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			
			//String qsPath = "[DATA].[S_SAMPLETYPE]";
			//String Exp = "<refobj>[FINAL].[S_SAMPLE].[SAMPLETYPEID]</refobj> = 'Production Sample'";
			String handle2 = "/O/definition[0]/modelQuery[0]/filters[0]/filterDefinition[0]/expression[0]/O/" + qsPath;
			String handle3 = "/O/definition[0]/modelQuery[0]/filters[0]/filterDefinition[0]/@apply/O/" + qsPath;
			String handle4 = "/O/definition[0]/modelQuery[0]/filters[0]/filterDefinition[0]/@application/O/" + qsPath;
			
			Element elemQsPath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element elemHandle2 = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"2\"]/inputparams/param[1]/value");
			Element elemExp = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"2\"]/inputparams/param[2]/value");
			Element elemHandle3 = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"3\"]/inputparams/param[1]/value");
			Element elemHandle4 = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"4\"]/inputparams/param[1]/value");
			
			elemQsPath.setText(qsPath);
			elemHandle2.setText(handle2);
			elemExp.setText(Exp);
			elemHandle3.setText(handle3);
			elemHandle4.setText(handle4);
			
//			System.out.println(document.asXML());
			System.out.println("createQuerySubjectFilter(" + qsPath + ", " + Exp + ")");
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}


	public void createQueryItem(String QuerySubject, String Name, String Exp) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createQueryItem.xml");
			File cdataFile = new File(csvc.getPathToXML() + "/createQueryItemCDATA.xml");
			SAXReader reader = new SAXReader();

			Document document = reader.read(xmlFile);
			Document cdata_document = reader.read(cdataFile);

			// xml
			Element qs = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723154520606\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element cdata = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723154520606\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			// cdata
			Element xp = (Element) cdata_document.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters/param[4]/expression");
			Element nm = (Element) cdata_document.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters//param[1]");
			Element root_cdata = cdata_document.getRootElement();

			qs.setText(QuerySubject);

			nm.addAttribute("value", Name);
			xp.setText(Exp);

			// attach cdata
			cdata.setText("");
			cdata.addCDATA(root_cdata.asXML());

			// System.out.println(document.asXML());
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	
	public void createQueryItemInFolder(String QS, String Folder, String Name, String Exp) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createQueryItemInFolder.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			// xml
			Element folder = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150729120206717\"]/transaction[@saved=\"false\"]/action[1]/inputparams/param[2]/value");
			Element nm = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150729120206717\"]/transaction[@saved=\"false\"]/action[2]/inputparams/param[2]/value");
			Element xp = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150729120206717\"]/transaction[@saved=\"false\"]/action[3]/inputparams/param[2]/value");

			Element n1 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150729120206717\"]/transaction[@saved=\"false\"]/action[2]/inputparams/param[1]/value");
			Element n2 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150729120206717\"]/transaction[@saved=\"false\"]/action[3]/inputparams/param[1]/value");

			// cdata
			n1.setText("/O/name[0]/O/" + QS + ".[New Query Item]");
			n2.setText("/O/expression[0]/O/" + QS + ".[" + Name + "]");
			folder.setText(QS + ".[" + Folder + "]");
			nm.setText(Name);
			xp.setText(Exp);

			// System.out.println(document.asXML());
//			System.out.println("createQueryItemInFolder(" + QS + ", " + Folder + ", " + Exp + ")");
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	
	public void changeQueryItemProperty(String queryItemPath, String property, String value) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/changeQueryitemProperty.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			// queryItem/hidden (false, true)
			// queryItem/usage (attribute, identifier, fact)
			// queryItem/displayType (value, picture, link)
//			String handleMappingpath = "queryItem/hidden";
//			String handleValue = "/O/hidden[0]/O/[DATA].[S_SAMPLE].[S_SAMPLEID]";
//			String value = "true";

			String handleMappingpath = "queryItem/" + property;
			String handleValue = "/O/" + property + "[0]/O/" + queryItemPath;

			// xml
			Element elemHandleMappingpath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action/inputparams/param[1]/mappingpath");
			Element elemHandleValue = (Element) document.selectSingleNode("/bmtactionlog/transaction/action/inputparams/param[1]/value");
			Element elemValue = (Element) document.selectSingleNode("/bmtactionlog/transaction/action/inputparams/param[2]/value");

			elemHandleMappingpath.setText(handleMappingpath);
			elemHandleValue.setText(handleValue);
			elemValue.setText(value);

//			 System.out.println(document.asXML());
//			System.out.println("changeQueryitemProperty(" + queryItemPath + ", " + property + ", " + value + ")");
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void createRelationship(RelationShip rs) {

		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createRelationship.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Element root = document.getRootElement();
			Element query_left = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[2]/inputparams/param[2]/value");
			Element query_right = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[5]/inputparams/param[2]/value");
			Element left_min = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[3]/inputparams/param[2]/value");
			Element left_max = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[4]/inputparams/param[2]/value");

			Element refobj_right_min = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[6]/inputparams/param[1]/value");
			Element refobj_right_max = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[7]/inputparams/param[1]/value");
			Element refobj_nm = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[8]/inputparams/param[1]/value");
			Element refobj_xp = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[9]/inputparams/param[1]/value");
			Element refobj_cr = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[10]/inputparams/param[1]/value");

			Element right_min = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[6]/inputparams/param[2]/value");
			Element right_max = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[7]/inputparams/param[2]/value");
			Element exp = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[9]/inputparams/param[2]/value");
			Element nm = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723131838883\"]/transaction[@saved=\"false\"]/action[8]/inputparams/param[2]/value");

			query_left.setText(rs.getQuerySubject_left());
			query_right.setText(rs.getQuerySubject_right());
			left_min.setText(rs.getCard_left_min());
			right_min.setText(rs.getCard_right_min());
			left_max.setText(rs.getCard_left_max());
			right_max.setText(rs.getCard_right_max());

			refobj_right_min.setText("/O/right[0]/mincard[0]/O/[" + rs.getParentNamespace() + "].[New Relationship]");
			refobj_right_max.setText("/O/right[0]/maxcard[0]/O/[" + rs.getParentNamespace() + "].[New Relationship]");
			refobj_nm.setText("/O/name[0]/O/[" + rs.getParentNamespace() + "].[New Relationship]");

			refobj_xp.setText("/O/expression[0]/O/[" + rs.getParentNamespace() + "].[" + rs.getName() + "]");
			refobj_cr.setText("[" + rs.getParentNamespace() + "].[" + rs.getName() + "]");
			nm.setText(rs.getName());
			exp.setText(rs.getExpression());
			System.out.println( "createRelationship - " +
					"query_left: " + rs.getQuerySubject_left() + ", " +
					"query_right: " + rs.getQuerySubject_right() + ", " +
					"left_min: " + rs.getCard_left_min() + ", " +
					"right_min: " + rs.getCard_right_min() + ", " +
					"left_max: " + rs.getCard_left_max() + ", " +
					"right_max: " + rs.getCard_right_max() + ", " +
					"parentNamespace: " + rs.getParentNamespace() + ", " +
					"name: " + rs.getName() + ", " +
					"exp: " + rs.getExpression()
					);
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}

	}


	public void ReorderSubFolderBefore(String handle, String target) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/ReorderBefore.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node tp = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723154806075\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/mappingpath");
			Node h = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723154806075\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node t = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723154806075\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");

			h.setText(handle);
			t.setText(target);
			tp.setText("queryItemFolder");

			System.out.println("ReorderSubFolderBefore(" + handle + ", " + target + ")");
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void ReorderBefore(String handle, String target) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/ReorderBefore.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node h = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723154806075\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Node t = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150723154806075\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");

			h.setText(handle);
			t.setText(target);

			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void createSubFolder(String QSRejObj, String Name) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createSubFolder.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Node n1 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150727152555245\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			n1.setText(QSRejObj);

			Node n2 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150727152555245\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[3]/value");
			n2.setText(Name);

			System.out.println("createSubFolder(" + QSRejObj + ", " + Name + ")");
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void createSubFolderInSubFolder(String QSRefObj, String Fext, String Fint) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createSubFolder.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Node n0 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150727152555245\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			n0.setText(QSRefObj + ".[" + Fext + "]");

			Node n2 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150727152555245\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[3]/value");
			n2.setText(Fint);

			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	public void createSubFolderInSubFolderIIC(String QSRefObj, String Fint) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createSubFolder.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Node n0 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150727152555245\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			n0.setText(QSRefObj);

			Node n2 = document.selectSingleNode("/bmtactionlog[@timestamp=\"20150727152555245\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[3]/value");
			n2.setText(Fint);

			System.out.println("createSubFolderInSubFolderIIC( " + QSRefObj + ", " + Fint + ")");
			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void createFilter(String Name, String Expression, String Namespace) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/createFilter.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node NamespaceNode = document.selectSingleNode("/bmtactionlog/transaction[1]/action[1]/inputparams[1]/param[2]/value[1]");
			Node NameNode = document.selectSingleNode("/bmtactionlog/transaction[1]/action[1]/inputparams[1]/param[3]/value[1]");
			Node ExpressionNode = document.selectSingleNode("/bmtactionlog/transaction[1]/action[3]/inputparams[1]/param[2]/value[1]");
			Node ExpressionSettingNode = document.selectSingleNode("/bmtactionlog/transaction[1]/action[3]/inputparams[1]/param[1]/value[1]");

			NamespaceNode.setText("[" + Namespace + "]");
			NameNode.setText(Name);
			ExpressionNode.setText(Expression);
			ExpressionSettingNode.setText("/O/expression[0]/O/[" + Namespace + "].[" + Name + "]");

			csvc.executeModel(document);

		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void assignFilter(String Filter, String QuerySubject) {
		try {

			File xmlFile = new File(csvc.getPathToXML() + "/assignFilter.xml");
			File cdataFile = new File(csvc.getPathToXML() + "/assignFilterCDATA.xml");
			SAXReader reader = new SAXReader();

			Document document = reader.read(xmlFile);
			Document cdata_document = reader.read(cdataFile);

			// xml
			Element qs = (Element) document.selectSingleNode("/bmtactionlog/transaction[1]/action[1]/inputparams[1]/param[1]/value[1]");
			Element cdata = (Element) document.selectSingleNode("/bmtactionlog/transaction[1]/action[1]/inputparams[1]/param[2]/value[1]");

			// cdata
			Element eFilter = (Element) cdata_document.selectSingleNode("/updateObjectRequest/tasks[1]/task[1]/parameters[1]/param[2]/refobj[1]");

			Element root_cdata = cdata_document.getRootElement();

			qs.setText(QuerySubject);
			eFilter.setText(Filter);

			// attach cdata
			cdata.setText("");
			cdata.addCDATA(root_cdata.asXML());

			// System.out.println(document.asXML());
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void lg(String msg) {
		Logger.logInfo(" BuildModel.java ", msg);
	}
	
	public void createScreenTip(String objectType, String objectPath, String objectToolTip) {
		
		try {
//			String objectType = "querySubject"; //queryItem , queryItemFolder
//			String objectPath = "[DATA].[S_SAMPLE].[S_SAMPLEID]";
//			String objectToolTip = "the SAMPLE !";
			
			String objectTypePath = objectType + "/screenTip";
			String stObjectPath = "/O/screenTip[0]/O/" + objectPath;

			File xmlFile = new File(csvc.getPathToXML() + "/createScreenTip.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element elemObjectType = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[1]/mappingpath");
			Element elemObjectPath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element elemObjectToolTip = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[2]/value");

			elemObjectType.setText(objectTypePath);
			elemObjectPath.setText(stObjectPath);
			elemObjectToolTip.setText(objectToolTip);
			
//			System.out.println(document.asXML());
//			System.out.println("createScreenTip(" + objectType + ", " + objectPath + ", " + objectToolTip + ")");
			csvc.executeModel(document);
			} catch (DocumentException ex) {
				lg(ex.getMessage());
			}
		}
	
	// Dimension functions factory
	public void createMeasureDimension(String handle, String measureDimensionName) {
		try {
	
			File xmlFile = new File(csvc.getPathToXML() + "/createMeasureDimension.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			String mappingPath = "";
			if (handle.contains("].[")) {mappingPath = "folder";} else {mappingPath="namespace";}

			Element elemMappingPath = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/mappingpath");
			Element elemHandle = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element cdata = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");

			String s = cdata.getText();
			elemMappingPath.setText(mappingPath);
			elemHandle.setText(handle);
			
			Document doc = reader.read(new StringReader(s));

			Element mdn = (Element) doc.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters//param[1]");
			Element root_cdata = doc.getRootElement();

			mdn.addAttribute("value", measureDimensionName);

			// attach cdata
		    cdata.setText("");
			cdata.addCDATA(root_cdata.asXML());

			// System.out.println(document.asXML());
			System.out.println("createMeasureDimension: handle " + handle + ", measureDimensionName " + measureDimensionName);
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void createMeasure(String dimensionPath, String measureName, String exp) {
		try {
			
//			String measureName = "SDIDATA_COUNT";
//			String dimensionPath = "[DIMENSIONS].[SDIDATA_MEASURES]";
//			String exp = "[FINAL].[SDIDATA].[SDIDATA_COUNT]";
						
			File xmlFile = new File(csvc.getPathToXML() + "/createMeasure.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element cdata = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");

			String s = cdata.getText();
			handle.setText(dimensionPath);
			
			Document doc = reader.read(new StringReader(s));

			Element mn = (Element) doc.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters//param[1]");
			Element xp = (Element) doc.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters/param[4]/expression/refobj");
			
			Element root_cdata = doc.getRootElement();

			mn.addAttribute("value", measureName);
			xp.setText(exp);

			// attach cdata
		    cdata.setText("");
			cdata.addCDATA(root_cdata.asXML());

			// System.out.println(document.asXML());
			System.out.println("createMeasure: dimensionPath " + dimensionPath + ", measureName " + measureName + ", exp " + exp);
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void createDimension(String handle, String dimensionName) {
		try {
			
//			namespace = "[DIMENSIONS]";
//			optionnalFolder = "";
//			dimensionName = "S_PRODUCT";
						
			String mappingPath = "";
			if (handle.contains("].[")) {mappingPath = "folder";} else {mappingPath="namespace";}
			
			File xmlFile = new File(csvc.getPathToXML() + "/createDimension.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element elemMappingPath = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/mappingpath");			
			Element handle1 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element cdata1 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			String s1 = cdata1.getText();
			elemMappingPath.setText(mappingPath);
			handle1.setText(handle);
			Document doc1 = reader.read(new StringReader(s1));
			Element dn = (Element) doc1.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters//param[1]");
			Element root_cdata1 = doc1.getRootElement();
			dn.addAttribute("value", dimensionName);		
			// attach cdata
		    cdata1.setText("");
		    cdata1.addCDATA(root_cdata1.asXML());

			// System.out.println(document.asXML());
		    System.out.println("createDimension : handle " + handle + ", dimensionName " + dimensionName);
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	
	public void moveDimension(String dimensionPath, String folderPath) {
		try {
			
			File xmlFile = new File(csvc.getPathToXML() + "/moveDimension.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle1 = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element handle2 = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[2]/value");
			handle1.setText(folderPath);
			handle2.setText(dimensionPath);
		    
			// System.out.println(document.asXML());
			System.out.println("moveDimension( dimensionPath " + dimensionPath + ", folderPath :" + folderPath);
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	
	public void createDimensionHierarchy(String dimensionPath, String exp) {
		try {
			
//			dimensionPath = "[DIMENSIONS].[S_PRODUCT]";
//			exp = "[FINAL].[S_PRODUCT]";
						
			File xmlFile = new File(csvc.getPathToXML() + "/createDimensionHierarchy.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
	    
			Element handle2 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element cdata2 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			String s2 = cdata2.getText();
			handle2.setText(dimensionPath);
			Document doc2 = reader.read(new StringReader(s2));
			Element xp = (Element) doc2.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters/param[3]/expression/refobj");		
			Element root_cdata2 = doc2.getRootElement();
			xp.setText(exp);
			// attach cdata
		    cdata2.setText("");
		    cdata2.addCDATA(root_cdata2.asXML());

			// System.out.println(document.asXML());
		    
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void addHierarchyLevel(String hierarchyPath, String levelBefore, String newLevel, String exp) {
		try {
			
//			hierarchyPath = "[DIMENSIONS].[S_PRODUCT].[S_PRODUCT]";
//			levelBefore = "[DIMENSIONS].[S_PRODUCT].[S_PRODUCT].[S_PRODUCT]";
//			newLevel = "[DIMENSIONS].[S_PRODUCT].[S_PRODUCT].[S_BATCH]";
//			exp = "[FINAL].[S_BATCH]";

			File xmlFile = new File(csvc.getPathToXML() + "/addHierarchyLevel.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element cdata = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			Element lb = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[1]/value");
			Element nl = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[2]/value");
			String s = cdata.getText();
			handle.setText(hierarchyPath);
			lb.setText(levelBefore);
			nl.setText(newLevel);
			Document doc = reader.read(new StringReader(s));
	//		Element dn = (Element) doc.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters//param[1]");
			Element xp = (Element) doc.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters/param[3]/expression/refobj");		
			Element root_cdata = doc.getRootElement();
//			dn.addAttribute("value", dimensionName);
			xp.setText(exp);
			// attach cdata
		    cdata.setText("");
		    cdata.addCDATA(root_cdata.asXML());
		    
			// System.out.println(document.asXML());
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void addDimensionHierarchy(String dimensionPath, String exp) {
		try {
			
//			dimensionPath = "[DIMENSIONS].[S_PRODUCT]";
//			exp = "[FINAL].[S_REQUEST]";

			File xmlFile = new File(csvc.getPathToXML() + "/addDimensionHierarchy.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171116140403836\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element cdata = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171116140403836\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			String s = cdata.getText();
			handle.setText(dimensionPath);
			Document doc = reader.read(new StringReader(s));
			Element xp = (Element) doc.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters/param[3]/expression/refobj");		
			Element root_cdata = doc.getRootElement();

			xp.setText(exp);
			// attach cdata
		    cdata.setText("");
		    cdata.addCDATA(root_cdata.asXML());
			
		    
			// System.out.println(document.asXML());
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	
	public void createDimensionRole_BK(String queryItemPath) {
		try {
			
//			String queryItemPath = "[DIMENSIONS].[S_PRODUCT].[S_PRODUCT].[S_PRODUCT].[S_PRODUCTID1]";

			File xmlFile = new File(csvc.getPathToXML() + "/createDimensionRole_BK.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle1 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element handle2 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[1]/value");
			handle1.setText(queryItemPath);
			handle2.setText(queryItemPath);
		    
			// System.out.println(document.asXML());
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	
	public void createDimensionRole_MC(String queryItemPath) {
		try {
			
//			String queryItemPath = "[DIMENSIONS].[S_PRODUCT].[S_PRODUCT].[S_PRODUCT].[S_PRODUCTID1]";

			File xmlFile = new File(csvc.getPathToXML() + "/createDimensionRole_MC.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle1 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element handle2 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[1]/value");
			handle1.setText(queryItemPath);
			handle2.setText(queryItemPath);
		    
			// System.out.println(document.asXML());
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	
	public void createDimensionRole_MD(String queryItemPath) {
		try {
			
//			String queryItemPath = "[DIMENSIONS].[S_PRODUCT].[S_PRODUCT].[S_PRODUCT].[S_PRODUCTID1]";

			File xmlFile = new File(csvc.getPathToXML() + "/createDimensionRole_MD.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle1 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element handle2 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[1]/value");
			handle1.setText(queryItemPath);
			handle2.setText(queryItemPath);
		    
			// System.out.println(document.asXML());
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}
	
	public void createScopeRelationship(String dimensionPath) {
				
			try {
				
//				String dimensionPath = "[DIMENSIONS].[S_PRODUCT]";

				File xmlFile = new File(csvc.getPathToXML() + "/createScopeRelationship.xml");
				SAXReader reader = new SAXReader();
				Document document = reader.read(xmlFile);

				Element handle = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171115164101779\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
				handle.setText(dimensionPath);
		    
			// System.out.println(document.asXML());
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			lg(ex.getMessage());
		}
	}

	public void adjustScopeRelationship(String dimensionMeasurePath, String measurePath, String dimensionPath, String levelPath, String int0or1) {
		
		try {
//			String dimensionMeasurePath = "[DIMENSIONS].[S_SAMPLE_MEASURES]";
//			String measurePath = "[DIMENSIONS].[S_SAMPLE_MEASURES].[S_SAMPLE_COUNT]";
//			String dimensionPath = "[DIMENSIONS].[S_PRODUCT]";
//			String levelPath = "[DIMENSIONS].[S_PRODUCT].[S_PRODUCT].[S_BATCH]";

			File xmlFile = new File(csvc.getPathToXML() + "/adjustScopeRelationship.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handleDMP = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171116145857971\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element handleMP = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171116145857971\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[3]/value");
			Element handleDP = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171116145857971\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			Element handleLP = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171116145857971\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[4]/value");
			Element elem0or1 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171116145857971\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[5]/value");
			
			handleDMP.setText(dimensionMeasurePath);
			handleMP.setText(measurePath);
			handleDP.setText(dimensionPath);
			handleLP.setText(levelPath);
			elem0or1.setText(int0or1);
			
		// System.out.println(document.asXML());
		csvc.executeModel(document);
	} catch (DocumentException ex) {
		lg(ex.getMessage());
	}
}
	
	public void createEmptyHierarchy(String dimensionPath, String hierarchyName) {
		
		try {
//			String dimensionPath = "[DIMENSIONS].[SDIDATA.CREATEDT]";
//			String hierarchyName = "SDIDATA.CREATEDT (By month)";
			
			String handleHierarchyName = "/O/name[0]/O/" + dimensionPath + ".[New Hierarchy]";
			String handleLevelName = "/O/name[0]/O/" + dimensionPath + ".[" + hierarchyName + "].[New Hierarchy(All)]";
			String levelName = hierarchyName + "(All)";

			File xmlFile = new File(csvc.getPathToXML() + "/createEmptyHierarchy.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle1 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122155500394\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element ElemhierarchyName = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122155500394\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			Element handle2 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122155500394\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[1]/value");
			Element ElemlevelName = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122155500394\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[2]/value");

			handle1.setText(handleHierarchyName);
			ElemhierarchyName.setText(hierarchyName);
			handle2.setText(handleLevelName);
			ElemlevelName.setText(levelName);
			
		// System.out.println(document.asXML());
		csvc.executeModel(document);
	} catch (DocumentException ex) {
		lg(ex.getMessage());
	}
}

	public void modifyHierarchyName(String dimensionPath, String hierarchyOldName, String hierarchyName) {
		
		try {
//			String dimensionPath = "[DIMENSIONS].[SDIDATA.CREATEDT]";
//			String hierarchyName = "SDIDATA.CREATEDT (By month)";
			
			String handleHierarchyName = "/O/name[0]/O/" + dimensionPath + ".[" + hierarchyOldName + "]";
			String handleLevelName = "/O/name[0]/O/" + dimensionPath + ".[" + hierarchyName + "].[" + hierarchyOldName +"(All)]";
			String levelName = hierarchyName + "(All)";
			
			File xmlFile = new File(csvc.getPathToXML() + "/modifyHierarchyName.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle1 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122155500394\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element ElemhierarchyName = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122155500394\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			Element handle2 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122155500394\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[1]/value");
			Element ElemlevelName = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122155500394\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[2]/value");

			handle1.setText(handleHierarchyName);
			ElemhierarchyName.setText(hierarchyName);
			handle2.setText(handleLevelName);
			ElemlevelName.setText(levelName);
			
		// System.out.println(document.asXML());
		csvc.executeModel(document);
	} catch (DocumentException ex) {
		lg(ex.getMessage());
	}
}

	
	public void createEmptyNewHierarchy(String dimensionPath) {
		
		try {
//			String dimensionPath = "[DIMENSIONS].[SDIDATA.CREATEDT]";
			
			File xmlFile = new File(csvc.getPathToXML() + "/createEmptyNewHierarchy.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122201636874\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			handle.setText(dimensionPath);
		
		// System.out.println(document.asXML());
		csvc.executeModel(document);
			} catch (DocumentException ex) {
				lg(ex.getMessage());
			}
		}
	
	public void createEmptyHierarchyLevel(String hierarchyPath, String levelName) {
		
		try {
			
//			String hierarchyPath = "[DIMENSIONS].[SDIDATA.CREATEDT].[SDIDATA.CREATEDT (By month)]";
//			String levelName = "YEAR";
					
			String handleLevelName = "/O/name[0]/O/" + hierarchyPath + ".[New Level]";
			
			File xmlFile = new File(csvc.getPathToXML() + "/createEmptyHierarchyLevel.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122161606340\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			Element elemhandleLevelName = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122161606340\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[1]/value");
			Element ElemlevelName = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122161606340\"]/transaction[@saved=\"false\"]/action[@seq=\"2\"]/inputparams/param[2]/value");
		
			handle.setText(hierarchyPath);
			elemhandleLevelName.setText(handleLevelName);
			ElemlevelName.setText(levelName);

		// System.out.println(document.asXML());
				csvc.executeModel(document);
			} catch (DocumentException ex) {
				lg(ex.getMessage());
			}
		}

	public void modify(String handleMappingpath, String handleValue, String value) {
		
		try {
			
			// handleMappingpath = "measure/regularAggregate";
			// handleValue = "/O/regularAggregate[0]/O/[DIMENSIONAL].[SDIDATA Fact].[SDCID]";
			// value = "count";
			
			File xmlFile = new File(csvc.getPathToXML() + "/modify.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Element elemHandleMappingpath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[1]/mappingpath");
			Element elemHandleValue = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element ElemValue = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[2]/value");
		
			elemHandleMappingpath.setText(handleMappingpath);
			elemHandleValue.setText(handleValue);
			ElemValue.setText(value);

		// System.out.println(document.asXML());
				System.out.println("modify( " + handleMappingpath + ", " + handleValue + ", " + value + ") ");
				csvc.executeModel(document);
			} catch (DocumentException ex) {
				lg(ex.getMessage());
			}
		}
	
	public void modifyLevelName(String hierarchyPath, String levelOldName, String levelName) {
		
		try {
			
//			String hierarchyPath = "[DIMENSIONS].[SDIDATA.CREATEDT].[SDIDATA.CREATEDT (By month)]";
//			String levelName = "YEAR";
					
			String handleLevelName = "/O/name[0]/O/" + hierarchyPath + ".[" + levelOldName + "]";
			
			File xmlFile = new File(csvc.getPathToXML() + "/modifyLevelName.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Element elemhandleLevelName = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122161606340\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element ElemlevelName = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122161606340\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
		
			elemhandleLevelName.setText(handleLevelName);
			ElemlevelName.setText(levelName);

		// System.out.println(document.asXML());
				csvc.executeModel(document);
			} catch (DocumentException ex) {
				lg(ex.getMessage());
			}
		}

	
	public void createHierarchyLevelQueryItem(String levelPath, String queryItemName, String exp) {
		
		try {
//			String levelPath = "[DIMENSIONS].[SDIDATA.CREATEDT].[SDIDATA.CREATEDT (By month)].[YEAR]";
//			String queryItemName = "YEAR_KEY";
//			String exp = "_year (<refobj>[FINAL].[SDIDATA].[CREATEDT]</refobj>)";
					
			File xmlFile = new File(csvc.getPathToXML() + "/createHierarchyLevelQueryItem.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122162251466\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
						
			Element cdata = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171122162251466\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			String s = cdata.getText();
			handle.setText(levelPath);

			Document doc = reader.read(new StringReader(s));
			Element qin = (Element) doc.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters//param[1]");
			Element xp = (Element) doc.selectSingleNode("/updateObjectRequest/tasks/task[@name=\"addObject\"]/parameters/param[4]/expression");		
			Element root_cdata = doc.getRootElement();
			qin.addAttribute("value", queryItemName);
			xp.setText(exp);
			// attach cdata
		    cdata.setText("");
		    cdata.addCDATA(root_cdata.asXML());

		// System.out.println(document.asXML());
				csvc.executeModel(document);
			} catch (DocumentException ex) {
				lg(ex.getMessage());
			}
		}

	public void modifyLevelQueryItemName(String levelPath, String queryItemOldName, String queryItemName, String exp) {
		
		try {
//			String queryItemOldName = "CREATEDT";
//			String queryItemName = "YEAR_KEY";
//			String exp = "_year(&lt;refobj&gt;[FINAL].[SDIDATA].[CREATEDT]&lt;/refobj&gt;)";
//			levelPath = "[DIMENSIONS].[SDIDATA.CREATEDT].[SDIDATA.CREATEDT (By month)].[YEAR]";

			File xmlFile = new File(csvc.getPathToXML() + "/modifyLevelQueryItem.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Element handle1 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171127165154093\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[1]/value");
			Element qiName = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171127165154093\"]/transaction[@saved=\"false\"]/action[@seq=\"1\"]/inputparams/param[2]/value");
			Element handle2 = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171127165154093\"]/transaction[@saved=\"false\"]/action[@seq=\"4\"]/inputparams/param[1]/value");
			Element qiExp = (Element) document.selectSingleNode("/bmtactionlog[@timestamp=\"20171127165154093\"]/transaction[@saved=\"false\"]/action[@seq=\"4\"]/inputparams/param[2]/value");
			
			handle1.setText("/O/name[0]/O/" + levelPath + ".[" + queryItemOldName + "]");
			qiName.setText(queryItemName);
			handle2.setText("/O/expression[0]/O/" + levelPath + ".[" + queryItemName + "]");
			qiExp.setText(exp);
			

		// System.out.println(document.asXML());
				csvc.executeModel(document);
			} catch (DocumentException ex) {
				lg(ex.getMessage());
			}
		}
	
	public void createTimeDimension(String dateQueryItemPath, String dimensionName, String dateQueryItemName) {
	

	// time dimension
//			String dateQueryItemPath = "[FINAL].[SDIDATA].[CREATEDT]";
//			String dateQueryItemName = "CREATEDT";
//			String dimensionName = "SDIDATA.CREATEDT";	
			
			createDimension("[DIMENSIONAL]", dimensionName);

	// hierarchy (By month)
			createDimensionHierarchy("[DIMENSIONAL].[" + dimensionName + "]", dateQueryItemPath);
			createScopeRelationship("[DIMENSIONAL].[" + dimensionName + "]");
		
	// level year
			modifyHierarchyName("[DIMENSIONAL].[" + dimensionName + "]", dateQueryItemName,dateQueryItemName + " (By month)");
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", dateQueryItemName, "YEAR");
			String exp = "_year(" + dateQueryItemPath + ")";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[YEAR]", dateQueryItemName, "YEAR_KEY", exp);
			exp = "_year (" + dateQueryItemPath + ")";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[YEAR]", "YEAR", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[YEAR].[YEAR]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[YEAR].[YEAR]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[YEAR].[YEAR_KEY]");

	//level quarter	
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[YEAR]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", dateQueryItemName, "QUARTER");
			exp = "if (_month (  " + dateQueryItemPath + " ) in (1,2,3)) then (1)  else (\n" + 
			"if (_month (  " + dateQueryItemPath + " ) in (4,5,6)) then (2)  else (\n" + 
			"if (_month (  " + dateQueryItemPath + " ) in (7,8,9)) then (3)  else (\n" + 
			"if (_month (  " + dateQueryItemPath + " ) in (10,11,12)) then (4)  else (0)\n" + 
			")))";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[QUARTER]", dateQueryItemName, "QUARTER_KEY", exp);
			exp = "_year (" + dateQueryItemPath + ") + '/' +\n" + 
					"cast (if (_month (  " + dateQueryItemPath + " ) in (1,2,3)) then (1)  else (\n" + 
					"if (_month (  " + dateQueryItemPath + " ) in (4,5,6)) then (2)  else (\n" + 
					"if (_month (  " + dateQueryItemPath + " ) in (7,8,9)) then (3)  else (\n" + 
					"if (_month (  " + dateQueryItemPath + " ) in (10,11,12)) then (4)  else (0)\n" + 
					"))),varchar)";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[QUARTER]", "QUARTER", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[QUARTER].[QUARTER]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[QUARTER].[QUARTER]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[QUARTER].[QUARTER_KEY]");

	// level month
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[QUARTER]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", dateQueryItemName, "MONTH");
			exp = "_month (" + dateQueryItemPath + ")";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[MONTH]", dateQueryItemName, "MONTH_KEY", exp);
			exp = "cast (" + dateQueryItemPath + ",varchar(7))";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[MONTH]", "MONTH", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[MONTH].[MONTH]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[MONTH].[MONTH]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[MONTH].[MONTH_KEY]");
			
	// level day
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[MONTH]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", dateQueryItemName, "DAY");
			exp =  "_day (" + dateQueryItemPath + ")";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DAY]", dateQueryItemName, "DAY_KEY", exp);
			exp = "cast (" + dateQueryItemPath + ",varchar(10))";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DAY]", "DAY", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DAY].[DAY]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DAY].[DAY]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DAY].[DAY_KEY]");
							
	// level AM/PM
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DAY]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", dateQueryItemName, "AM/PM");
			exp = "if (_hour (  " + dateQueryItemPath + " ) in_range {0:11}) then (1)  else (2)";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[AM/PM]", dateQueryItemName, "AM/PM_KEY", exp);
			exp = "cast (" + dateQueryItemPath + ",varchar(10)) + ' ' +\n" + 
					"if (_hour (  " + dateQueryItemPath + " ) in_range {0:11}) then ('AM')  else ('PM')";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[AM/PM]", "AM/PM", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[AM/PM].[AM/PM]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[AM/PM].[AM/PM]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[AM/PM].[AM/PM_KEY]");

	// level hour
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[AM/PM]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", dateQueryItemName, "HOUR");
			exp = "_hour (  " + dateQueryItemPath + " )";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[HOUR]", dateQueryItemName, "HOUR_KEY", exp);
			exp = "cast (" + dateQueryItemPath + ",varchar(13))";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[HOUR]", "HOUR", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[HOUR].[HOUR]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[HOUR].[HOUR]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[HOUR].[HOUR_KEY]");

	// level date
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[HOUR]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)]", dateQueryItemName, "DATE");
			exp = dateQueryItemPath;
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DATE]", dateQueryItemName, "DATE_KEY", exp);
			exp = "cast (" + dateQueryItemPath + ",varchar(19))";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DATE]", "DATE", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DATE].[DATE]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DATE].[DATE]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By month)].[DATE].[DATE_KEY]");
			
	// hierarchy by week
			addDimensionHierarchy("[DIMENSIONAL].[" + dimensionName + "]", dateQueryItemPath);
			modifyHierarchyName("[DIMENSIONAL].[" + dimensionName + "]", dateQueryItemName,dateQueryItemName + " (By week)");
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", dateQueryItemName, "YEAR");
			exp = "_year(" + dateQueryItemPath + ")";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[YEAR]", dateQueryItemName, "YEAR_KEY", exp);
			exp = "_year (" + dateQueryItemPath + ")";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[YEAR]", "YEAR", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[YEAR].[YEAR]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[YEAR].[YEAR]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[YEAR].[YEAR_KEY]");

	//level week	
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[YEAR]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", dateQueryItemName, "WEEK");
			exp = "_week_of_year (" + dateQueryItemPath + ")";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[WEEK]", dateQueryItemName, "WEEK_KEY", exp);
			exp = "cast(_year (" + dateQueryItemPath + ") ,varchar(4))+ '/' +\n" + 
					"if (_week_of_year (" + dateQueryItemPath + ") < 10 ) then ('0') else ('') + \n" + 
					"cast(_week_of_year (" + dateQueryItemPath + "),varchar(2))";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[WEEK]", "WEEK", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[WEEK].[WEEK]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[WEEK].[WEEK]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[WEEK].[WEEK_KEY]");

	//level day_of_week	
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[WEEK]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", dateQueryItemName, "DAY_OF_WEEK");
			exp = "_day_of_week (" + dateQueryItemPath + ",1)";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DAY_OF_WEEK]", dateQueryItemName, "DAY_OF_WEEK_KEY", exp);
			exp = "cast (" + dateQueryItemPath + ",varchar(10)) + ' ' +\n" + 
					"if (_day_of_week (" + dateQueryItemPath + ",1) = 1)\n" + 
					"then ('Mon') else (\n" + 
					"if (_day_of_week (" + dateQueryItemPath + ",1) = 2)\n" + 
					"then ('Tue') else (\n" + 
					"if (_day_of_week (" + dateQueryItemPath + ",1) = 3)\n" + 
					"then ('Wed') else (\n" + 
					"if (_day_of_week (" + dateQueryItemPath + ",1) = 4)\n" + 
					"then ('Thu') else (\n" + 
					"if (_day_of_week (" + dateQueryItemPath + ",1) = 5)\n" + 
					"then ('Fri') else (\n" + 
					"if (_day_of_week (" + dateQueryItemPath + ",1) = 6)\n" + 
					"then ('Sat') else ('Sun')\n" + 
					")))))";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DAY_OF_WEEK]", "DAY_OF_WEEK", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DAY_OF_WEEK].[DAY_OF_WEEK]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DAY_OF_WEEK].[DAY_OF_WEEK]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DAY_OF_WEEK].[DAY_OF_WEEK_KEY]");

	// level AM/PM
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DAY_OF_WEEK]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", dateQueryItemName, "AM/PM");
			exp = "if (_hour (  " + dateQueryItemPath + " ) in_range {0:11}) then (1)  else (2)";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[AM/PM]", dateQueryItemName, "AM/PM_KEY", exp);
			exp = "cast (" + dateQueryItemPath + ",varchar(10)) + ' ' +\n" + 
					"if (_hour (  " + dateQueryItemPath + " ) in_range {0:11}) then ('AM')  else ('PM')";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[AM/PM]", "AM/PM", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[AM/PM].[AM/PM]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[AM/PM].[AM/PM]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[AM/PM].[AM/PM_KEY]");

	// level hour
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[AM/PM]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", dateQueryItemName, "HOUR");
			exp = "_hour (  " + dateQueryItemPath + " )";
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[HOUR]", dateQueryItemName, "HOUR_KEY", exp);
			exp = "cast (" + dateQueryItemPath + ",varchar(13))";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[HOUR]", "HOUR", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[HOUR].[HOUR]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[HOUR].[HOUR]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[HOUR].[HOUR_KEY]");

	// level date
			addHierarchyLevel("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[HOUR]", "[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[CREATEDT]", dateQueryItemPath);
			modifyLevelName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)]", dateQueryItemName, "DATE");
			exp = dateQueryItemPath;
			modifyLevelQueryItemName("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DATE]", dateQueryItemName, "DATE_KEY", exp);
			exp = "cast (" + dateQueryItemPath + ",varchar(19))";
			createHierarchyLevelQueryItem("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DATE]", "DATE", exp);
			createDimensionRole_MC("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DATE].[DATE]");
			createDimensionRole_MD("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DATE].[DATE]");
			createDimensionRole_BK("[DIMENSIONAL].[" + dimensionName + "].[" + dateQueryItemName + " (By week)].[DATE].[DATE_KEY]");
			
	}
	
	public void recursiveParserQI(Document document, String spath, String locale, Map <String, String> map, String qsFinal) {
		
//		s = "/project/namespace/namespace[1]/namespace[1]/querySubject[1]";
		int i = 1;
		Element qiname = (Element) document.selectSingleNode(spath + "/queryItem[" + i + "]/name");
		Element qiNameLocale = (Element) document.selectSingleNode(spath + "/queryItem[" + i + "]/name[@locale=\"" + locale + "\"]");
		
		int j = 1;
		Element qifname = (Element) document.selectSingleNode(spath + "/queryItemFolder[" + j + "]/name");
		Element qifNameLocale = (Element) document.selectSingleNode(spath + "/queryItemFolder[" + j + "]/name[@locale=\"" + locale + "\"]");
		
		while (qifname != null)
		{
//			System.out.println(qifname.getStringValue() + " * * * * * * * " + map.get(qsFinal + qifname.getStringValue()));  // clef de map
			String label = map.get(qsFinal + qifname.getStringValue());  // valeur de map
			if (label != null) {
				qifNameLocale.setText(label);        // valeur de map
				}
			String nextsPath = spath + "/queryItemFolder[" + j + "]";
			recursiveParserQI(document, nextsPath, locale, map, qsFinal);
			j++;
			qifname = (Element) document.selectSingleNode(spath + "/queryItemFolder[" + j + "]/name");
			qifNameLocale = (Element) document.selectSingleNode(spath + "/queryItemFolder[" + j + "]/name[@locale=\"" + locale + "\"]");
		}
		
		while (qiname != null)
		{
//			System.out.println(qiname.getStringValue() + "****" + map.get(qsFinal + "." + qiname.getStringValue()));    // clef de map
			String label = map.get(qsFinal + "." + qiname.getStringValue());
			if (label != null) {
				qiNameLocale.setText(label);        // valeur de map
				}
			i++;
			qiname = (Element) document.selectSingleNode(spath + "/queryItem[" + i + "]/name");
			qiNameLocale = (Element) document.selectSingleNode(spath + "/queryItem[" + i + "]/name[@locale=\"" + locale + "\"]");
		}
	}
	
	public void recursiveParserQS(Document document, String spath, String locale, Map<String, String> map) {
		
		
//		spath = "/project/namespace/namespace[1]/namespace[1]";
		int i = 1;
		Element qsname = (Element) document.selectSingleNode(spath + "/querySubject[" + i + "]/name");
		Element qsNameLocale = (Element) document.selectSingleNode(spath + "/querySubject[" + i + "]/name[@locale=\"" + locale + "\"]");
		
		int j = 1;
		Element qsfname = (Element) document.selectSingleNode(spath + "/folder[" + j + "]/name");
		Element qsfNameLocale = (Element) document.selectSingleNode(spath + "/folder[" + j + "]/name[@locale=\"" + locale + "\"]");
				
		while (qsfname != null)
		{
//			System.out.println(qsfname.getStringValue());  // clef de map
//			qsfNameLocale.setText("qsfNameLocale" + j);               // valeur du map
			String nextFPath = spath + "/folder[" + j + "]";
			recursiveParserQS(document, nextFPath, locale, map);
			j++;
			qsfname = (Element) document.selectSingleNode(spath + "/folder[" + j + "]/name");
//			qsfNameLocale = (Element) document.selectSingleNode(spath + "/folder[" + j + "]/name[@locale=\"" + locale + "\"]");
		}
		
		while (qsname != null)
		{
			System.out.println(qsname.getStringValue() + " *    *     *     *     *    * " + map.get(qsname.getStringValue()));  // clef de map
			String label = map.get(qsname.getStringValue());
			if (label != null) {
			qsNameLocale.setText(label); // valeur de map
			}
			String nextQIPath = spath + "/querySubject[" + i + "]";
			recursiveParserQI(document, nextQIPath, locale, map, qsname.getStringValue());
			i++;
			qsname = (Element) document.selectSingleNode(spath + "/querySubject[" + i + "]/name");
			qsNameLocale = (Element) document.selectSingleNode(spath + "/querySubject[" + i + "]/name[@locale=\"" + locale + "\"]");
		}
	}
	
	public void createPackage(String packageName, String packageDescription, String packageScreenTip, String[] locales) {
		
		try {

			String securityViews = "[].[securityViews]";
		//	String[] locales = {"en"};
		//	String packageName = "DDTool";
		//	String packageDescription = "description test";
		//	String packageScreenTip = "Screen tip test";
			
			String securityViewsPath = securityViews + ".[" + packageName + "]";
			String packagesPath = "[].[packages]";

			File xmlFile = new File(csvc.getPathToXML() + "/createPackage.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			// action 1
			Element ElemSecurityViews = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[2]/value");
			Element ElemPackageName = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"1\"]/inputparams/param[3]/value");
			ElemSecurityViews.setText(securityViews);
			ElemPackageName.setText(packageName);
			// 2
			Element ElemSecurityViewsPath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"2\"]/inputparams/param[1]/value");
			ElemSecurityViewsPath.setText(securityViewsPath);
			//3
			Element ElemPackagesPath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"3\"]/inputparams/param[2]/value");
			ElemPackagesPath.setText(packagesPath);
			ElemPackageName = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"3\"]/inputparams/param[3]/value");
			ElemPackageName.setText(packageName);
			//4
			String packagePath = packagesPath + ".[" + packageName + "]";
			Element ElemPackagePath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"4\"]/inputparams/param[1]/value");
			ElemPackagePath.setText(packagePath);
			ElemSecurityViewsPath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"4\"]/inputparams/param[2]/value");
			ElemSecurityViewsPath.setText(securityViewsPath);
			//5
			ElemPackagePath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"5\"]/inputparams/param[1]/value");
			ElemPackagePath.setText(packagePath);
			Element Elemlanguage = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"5\"]/inputparams/param[2]/value");
			Elemlanguage.setText(locales[0]);
			//6
			String packageDescriptionPath = "/O/description[0]/O/" + packagePath;
			Element ElemPackageDescriptionPath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"6\"]/inputparams/param[1]/value");
			ElemPackageDescriptionPath.setText(packageDescriptionPath);

			Element ElemPackageDescription = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"6\"]/inputparams/param[2]/value");
			ElemPackageDescription.setText(packageDescription);
			//7
			String packageTooTipPath = "/O/screenTip[0]/O/" + packagePath;
			Element ElemPackageTooTipPathPath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"7\"]/inputparams/param[1]/value");
			ElemPackageTooTipPathPath.setText(packageTooTipPath);

			Element ElemPackageScreenTip = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"7\"]/inputparams/param[2]/value");
			ElemPackageScreenTip.setText(packageScreenTip);
			//8
			ElemSecurityViewsPath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"8\"]/inputparams/param[1]/value");
			ElemSecurityViewsPath.setText(securityViewsPath);
			//9
			ElemPackagePath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"9\"]/inputparams/param[1]/value");
			ElemPackagePath.setText(packagePath);
			//10
			ElemSecurityViewsPath = (Element) document.selectSingleNode("/bmtactionlog/transaction/action[@seq=\"10\"]/inputparams/param[1]/value");
			ElemSecurityViewsPath.setText(securityViewsPath);

//			System.out.println(document.asXML());
			csvc.executeModel(document);
			} catch (DocumentException ex) {
				lg(ex.getMessage());
			}
		}
	
	public void publishPackage(String packageName, String folder) {
		
		// create a folder in the public folders
		//folder = "/content/folder[@name='Folder DDTool']";
		csvc.createPublicFolder(folder);

		try {

			String securityViews = "[].[securityViews]";

		//	String packageName = "DDTool";
			
			String securityViewsPath = securityViews + ".[" + packageName + "]";
			String packagesPath = "[].[packages]";
			String packagePath = packagesPath + ".[" + packageName + "]";

			File xmlFile = new File(csvc.getPathToXML() + "/publishPackage.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			// transaction 1
			Element ElemSecurityViewsPath = (Element) document.selectSingleNode("/bmtactionlog/transaction[1]/action[@seq=\"1\"]/inputparams/param[1]/value");
			ElemSecurityViewsPath.setText(securityViewsPath);
			// 2
			Element ElemPackagePath = (Element) document.selectSingleNode("/bmtactionlog/transaction[2]/action[@seq=\"1\"]/inputparams/param[1]/value");
			ElemPackagePath.setText(packagePath);
			// 3
			ElemPackagePath = (Element) document.selectSingleNode("/bmtactionlog/transaction[3]/action[@seq=\"1\"]/inputparams/param[1]/value");
			ElemPackagePath.setText(packagePath);
			Element ElemPackageFolder = (Element) document.selectSingleNode("/bmtactionlog/transaction[3]/action[@seq=\"1\"]/inputparams/param[3]/value");
			ElemPackageFolder.setText(folder);
			Element ElemPackageName = (Element) document.selectSingleNode("/bmtactionlog/transaction[3]/action[@seq=\"1\"]/inputparams/param[4]/value");
			ElemPackageName.setText(packageName);
			
	//		System.out.println(document.asXML());
			csvc.executeModel(document);
			} catch (DocumentException ex) {
				lg(ex.getMessage());
			}
		}

}
