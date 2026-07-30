package net.oktawia.gtopt.ae2.gridservice;

import net.oktawia.gtopt.ae2.IStockingPartOpt;

import appeng.api.networking.IGridService;

public interface IStockingService extends IGridService {

    void markForRefresh(IStockingPartOpt part);

    void markForAutoPull(IStockingPartOpt part);
}
