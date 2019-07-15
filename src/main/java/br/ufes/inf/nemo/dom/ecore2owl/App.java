package br.ufes.inf.nemo.dom.ecore2owl;

import org.apache.jena.ext.com.google.common.collect.Maps.EntryTransformer;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreSwitch;
import org.eclipse.emf.ecore.util.Switch;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * Hello world!
 *
 */
public class App 
{
	
	protected ResourceSet resourceSet; 
	
	/**
	 * Create a new Ecore {@link ResourceSet} with support for ecore and xmi files. 
	 * @return
	 */
	public void initEcoreResourceSet() {
		//
		resourceSet = new ResourceSetImpl();
		// Register file factories corresponding to each extension
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new  XMIResourceFactoryImpl());
	}
	
	public Resource loadMetamodel(URI metamodelURI) {
		// Load metamodel from URI, if it's not already loaded, and return the resource corresponding to metamodel
		return resourceSet.getResource(metamodelURI, true);
	}
	
	/**
	 * Create an ontology model ({@link OntModel}) from given {@link EPackage} instance.
	 * @param epackage
	 * @return
	 */
	public OntModel ontologyModelFromEPackage(EPackage epackage) {
		return null;
	}
	
    public static void main( String[] args )
    {
    	new App().run(args);
    }

	private void run(String[] args) {
		initEcoreResourceSet();
		URI ecoreFile = URI.createFileURI("/home/luciano/Desenvolvimento/Mestrado/EMF/OWL/model/ecoreowl.ecore");
		Resource metamodel = loadMetamodel(ecoreFile);
		OntModel ontModel = OntologyBuilder.fromResource(metamodel);
		ontModel.write(System.out, "RDF/XML");
//		resourceSet.getPackageRegistry().put("?", epackage);
//		//
//		Resource model = resourceSet.getResource(modelURI, true);
	}
}
