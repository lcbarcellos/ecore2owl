package br.ufes.inf.nemo.dom.ecore2owl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreSwitch;

public class OntologyBuilder extends EcoreSwitch<OntModel> {

	final EPackage ePackage;
	final OntModel ontModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
	final List<ENamedElement> namedElementStack = new ArrayList<>();
	
	protected OntologyBuilder(EPackage pkg) {
		this.ePackage = pkg;
		assert !pkg.getNsPrefix().isEmpty();
		assert !pkg.getNsURI().isEmpty();
		this.ontModel.setNsPrefix(getNsPrefix(),  pkg.getNsURI());
	}

	/**
	 * Return namespace prefix to be used in Ontology Model. 
	 * By default the namespace prefix returned by {@link EPackage#getNsPrefix()} is used
	 * if not already present in Ontology Model. If it's not the case, a numbered suffix is
	 * added to the word returned by {@link EPackage#getNsPrefix()}.
	 * @return
	 */
	protected String getNsPrefix() {
		final String nsPrefix=ePackage.getNsPrefix();
		final Map<String, String> prefixMap = ontModel.getNsPrefixMap();
		if (!prefixMap.containsKey(nsPrefix)) {
			return nsPrefix;
		} else {
			String result;
			for (int suffix = -2; prefixMap.containsKey(result = nsPrefix + suffix); suffix--);
			return result;
		}
	}
	
	protected OntModel build() {
		ontModel.createOntology(ePackage.getNsURI());
		ePackage.eContents().forEach(this::doSwitch);
		return this.ontModel;
	}
	
	public static OntModel fromEPackage(EPackage ePackage) {		
		OntologyBuilder builder = new OntologyBuilder(ePackage);
		return builder.build();
	}
	
	/**
	 * Return an URI for an element based on context of navigation on model tree.
	 * @param eNamedElement
	 * @return
	 */
	protected String getName(ENamedElement eNamedElement) {
		final StringBuilder result = new StringBuilder(this.ePackage.getNsURI());
		result.append("#");
		if (!namedElementStack.isEmpty()) {
			result.append(namedElementStack.get(0).getName());
			namedElementStack
			.subList(1, namedElementStack.size())
			.forEach(elem -> {
				result.append("/");
				result.append(elem.getName());
			});
			result.append("/");
		}
		return result.append(eNamedElement.getName()).toString();
	}
	
	/**
	 * Process element contents with its name pushed into stack.
	 * @param eNamedElement
	 * @param action
	 */
	protected void runContentsWithName(ENamedElement eNamedElement, Consumer<? super EObject> action) {
		namedElementStack.add(eNamedElement);
		eNamedElement.eContents().forEach(action);
		namedElementStack.remove(namedElementStack.size()-1);
	}
	
	@Override
	public OntModel caseEClass(EClass eClass) {
    	ontModel.createClass(getName(eClass));
    	runContentsWithName(eClass, this::doSwitch);
		return super.caseEClass(eClass);
	}

	@Override
	public OntModel caseEAttribute(EAttribute eAttribute) {
		ontModel.createDatatypeProperty(getName(eAttribute));
		return super.caseEAttribute(eAttribute);	
	}
	
	@Override
	public OntModel caseEReference(EReference eReference) {
		ontModel.createObjectProperty(getName(eReference));
		return super.caseEReference(eReference);	
	}

	public static OntModel fromResource(Resource metamodel) {
		EcoreSwitch<OntModel> ecoreSwitch = new EcoreSwitch<OntModel>() {
			@Override
			public OntModel caseEPackage(EPackage ePackage) {
				return fromEPackage(ePackage);
			}			
		};	
		TreeIterator<EObject> iterator = metamodel.getAllContents();
		while (iterator.hasNext()) {
			OntModel result = ecoreSwitch.doSwitch(iterator.next());
			if (result != null) {
				return result;
			}
		}
		throw new RuntimeException("EPackage element not found in model");
	}	
}
