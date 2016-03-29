package com.pmease.gitplex.web.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.security.ObjectPermission;

@SuppressWarnings("serial")
public class AffinalDepotsModel extends LoadableDetachableModel<Collection<Depot>> {

	private final Long repoId;
	
	public AffinalDepotsModel(Long repoId) {
		this.repoId = repoId;
	}
	
	@Override
	protected Collection<Depot> load() {
		Depot depot = GitPlex.getInstance(Dao.class).load(Depot.class, repoId);;
		List<Depot> affinals = depot.findAffinals();
		affinals.remove(depot);
		if (depot.getForkedFrom() != null)
			affinals.remove(depot.getForkedFrom());
		affinals.sort((repo1, repo2) -> {
			if (repo1.getAccount().equals(repo2.getAccount()))
				return repo1.getName().compareTo(repo2.getName());
			else
				return repo1.getAccount().getName().compareTo(repo2.getAccount().getName());
		});
		if (depot.getForkedFrom() != null)
			affinals.add(0, depot.getForkedFrom());
		affinals.add(0, depot);
		for (Iterator<Depot> it = affinals.iterator(); it.hasNext();) {
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotRead(it.next())))
				it.remove();
		}
		return affinals;
	}

}
