package de.adito.beans.core.references;

import de.adito.beans.core.IBean;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.*;

/**
 * Beschreibt die hierarchische Struktur eines Bean-Elements (Bean oder Container)
 * Eine solche Struktur bezieht sich allein auf das Bean-Modell.
 * Sie entsteht nur dann, wenn ein Bean-Feld oder ein Container-Feld eine Referenz auf andere Bean-Elemente erzeugen.
 *
 * @author s.danner, 01.09.2017
 */
public interface IHierarchicalStructure
{
  /**
   * Liefert alle direkten Parent-Knoten dieser Struktur.
   * Ein Knoten beschreibt eine Referenz bestehend aus Bean und zugehörigem Feld.
   *
   * @return eine Menge von Hierarchie-Knoten dieser Struktur
   */
  Set<IHierarchicalNode> getDirectParents();

  /**
   * Vernichtet die Information zur hierarchischen Struktur dieses Bean-Elementes.
   * Ist evtl. sinnvoll, wenn für komplexe Berechnungen alte bzw. nicht mehr relevante Referenzen
   * nicht mehr miteinbezogen werden sollen (Falls der GC noch nicht aufgeräumt hat)
   */
  void destroy();

  /**
   * Beschreibt einen Hierarchie-Noten dieses Modells.
   * Beinhaltet die Bean und das Feld einer Referenz.
   * Eine Node ist iterierbar anhand derer Bean-Parent-Knoten.
   */
  interface IHierarchicalNode extends Iterable<IHierarchicalNode>
  {
    /**
     * Liefert die Bean des Knotens / dieser Stufe.
     */
    IBean<?> getBean();

    /**
     * Liefert das Bean-Feld des Knotens / dieser Stufe.
     */
    IHierarchicalField<?> getField();

    @NotNull
    @Override
    default Iterator<IHierarchicalNode> iterator()
    {
      return getBean().getHierarchicalStructure().getDirectParents().iterator();
    }

    /**
     * Liefert einen Stream der Parent-Nodes dieser Node.
     */
    default Stream<IHierarchicalNode> streamParentNodes()
    {
      return StreamSupport.stream(spliterator(), false);
    }
  }
}
