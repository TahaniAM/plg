package plg.io.exporter;

import java.io.File;
import java.io.IOException;

import org.deckfour.spex.SXDocument;
import org.deckfour.spex.SXTag;

import plg.annotations.Exporter;
import plg.io.importer.PLGImporter;
import plg.model.Process;
import plg.model.activity.Task;
import plg.model.data.DataObject;
import plg.model.data.GeneratedDataObject;
import plg.model.data.IntegerDataObject;
import plg.model.data.StringDataObject;
import plg.model.event.EndEvent;
import plg.model.event.StartEvent;
import plg.model.gateway.ExclusiveGateway;
import plg.model.gateway.Gateway;
import plg.model.gateway.ParallelGateway;
import plg.model.sequence.Sequence;
import plg.utils.Logger;
import plg.utils.PlgConstants;

/**
 * This class contains the exporter towards custom-defined file format
 * 
 * @author Andrea Burattin
 * @see PLGImporter
 */
@Exporter(
	name = "PLG 2 file",
	fileExtension = "plg"
)
public class PLGExporter implements IFileExporter {

	@Override
	public void exportModel(Process model, String filename) {
		Logger.instance().info("Starting process exportation");
		try {
			File file = new File(filename);
			SXDocument doc = new SXDocument(file);
			
			// header 
			doc.addComment("\n\n"
					+ "WARNING: Do not manually edit this file, unless you know what you are doing!\n\n"
					+ "This file has been generated with " + PlgConstants.libPLG_SIGNATURE + ".\n"
					+ "Check https://github.com/delas/plg for sources and other stuff.\n\n");
			
			// meta information
			SXTag process = doc.addNode("process");
			process.addComment("This is the list of all meta-attributes of the process");
			SXTag meta = process.addChildNode("meta");
			meta.addChildNode("LibPLG_NAME").addTextNode(PlgConstants.LibPLG_NAME);
			meta.addChildNode("libPLG_VERSION").addTextNode(PlgConstants.libPLG_VERSION);
			meta.addChildNode("name").addTextNode(model.getName());
			meta.addChildNode("id").addTextNode(model.getId());
			
			// process elements
			process.addComment("This is the list of all actual process elementss");
			SXTag elements = process.addChildNode("elements");
			
			for(StartEvent se : model.getStartEvents()) {
				SXTag seTag = elements.addChildNode("startEvent");
				seTag.addAttribute("id", se.getId());
				for (DataObject d : se.getDataObjects()) {
					seTag.addChildNode("dataObject").addAttribute("id", d.getId());
				}
			}
			for(Task t : model.getTasks()) {
				SXTag tTag = elements.addChildNode("task");
				tTag.addAttribute("id", t.getId());
				tTag.addAttribute("name", t.getName());
				for (DataObject d : t.getDataObjects()) {
					tTag.addChildNode("dataObject").addAttribute("id", d.getId());
				}
				SXTag script = tTag.addChildNode("script");
				if (t.getActivityScript() != null) {
					script.addCDataNode(t.getActivityScript().getScript());
				}
			}
			for(Gateway g : model.getGateways()) {
				SXTag gTag = elements.addChildNode("gateway");
				gTag.addAttribute("id", g.getId());
				if (g instanceof ExclusiveGateway) {
					gTag.addAttribute("type", "ExclusiveGateway");
				} else if (g instanceof ParallelGateway) {
					gTag.addAttribute("type", "ParallelGateway");
				}
				for (DataObject d : g.getDataObjects()) {
					gTag.addChildNode("dataObject").addAttribute("id", d.getId());
				}
			}
			for(EndEvent ee : model.getEndEvents()) {
				SXTag eeTag = elements.addChildNode("endEvent");
				eeTag.addAttribute("id", ee.getId());
				for (DataObject d : ee.getDataObjects()) {
					eeTag.addChildNode("dataObject").addAttribute("id", d.getId());
				}
			}
			for(Sequence s : model.getSequences()) {
				SXTag sTag = elements.addChildNode("sequenceFlow");
				sTag.addAttribute("id", s.getId());
				sTag.addAttribute("sourceRef", s.getSource().getId());
				sTag.addAttribute("targetRef", s.getSink().getId());
				for (DataObject d : s.getDataObjects()) {
					sTag.addChildNode("dataObject").addAttribute("id", d.getId());
				}
			}
			// data objects
			for(DataObject dobj : model.getDataObjects()) {
				SXTag dobjTag = elements.addChildNode("dataObject");
				dobjTag.addAttribute("id", dobj.getId());
				dobjTag.addAttribute("owner", dobj.getObjectOwner().getId());
				if (dobj instanceof StringDataObject) {
					dobjTag.addAttribute("name", dobj.getName() + "(string)");
					dobjTag.addAttribute("type", "StringDataObject");
					dobjTag.addChildNode("script").addCDataNode(((GeneratedDataObject) dobj).getScriptExecutor().getScript());
				} else if (dobj instanceof IntegerDataObject) {
					dobjTag.addAttribute("name", dobj.getName() + "(integer)");
					dobjTag.addAttribute("type", "IntegerDataObject");
					dobjTag.addChildNode("script").addCDataNode(((GeneratedDataObject) dobj).getScriptExecutor().getScript());
				} else {
					dobjTag.addAttribute("name", dobj.getName());
					dobjTag.addAttribute("value", (String) dobj.getValue());
					dobjTag.addAttribute("type", "DataObject");
				}
			}
			
			doc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Logger.instance().info("Process exportation complete");
	}
}
