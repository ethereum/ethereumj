/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.mine;

/**
 * {@link MinerListener} designed for use with {@link EthashMiner}
 */
public interface EthashListener extends MinerListener {

    enum DatasetStatus {
        /**
         * Dataset requested and will be prepared
         */
        DATASET_PREPARE,
        /**
         * Indicates start of light DAG generation
         * If full dataset is requested, its event
         * {@link #FULL_DATASET_GENERATE_START} fires before this one
         */
        LIGHT_DATASET_GENERATE_START,
        /**
         * Indicates that light dataset is already generated
         * and will be loaded from disk though it could be outdated
         * and therefore {@link #LIGHT_DATASET_LOADED} will not be fired
         */
        LIGHT_DATASET_LOAD_START,
        /**
         * Indicates end of loading light dataset from disk
         */
        LIGHT_DATASET_LOADED,
        /**
         * Indicates finish of light dataset generation
         */
        LIGHT_DATASET_GENERATED,
        /**
         * Indicates start of full DAG generation
         * Full DAG generation is a heavy procedure
         * which could take a lot of time.
         * Also full dataset requires light dataset
         * so it will be either generated or loaded from
         * disk as part of this job
         */
        FULL_DATASET_GENERATE_START,
        /**
         * Indicates that full dataset is already generated
         * and will be loaded from disk though it could be outdated
         * and therefore {@link #FULL_DATASET_LOADED} will not be fired
         */
        FULL_DATASET_LOAD_START,
        /**
         * Indicates end of full dataset loading from disk
         */
        FULL_DATASET_LOADED,
        /**
         * Indicates finish of full dataset generation
         */
        FULL_DATASET_GENERATED,
        /**
         * Requested dataset is complete and ready for use
         */
        DATASET_READY,
    }

    void onDatasetUpdate(DatasetStatus datasetStatus);
}
