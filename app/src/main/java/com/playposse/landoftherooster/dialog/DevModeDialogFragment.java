package com.playposse.landoftherooster.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.playposse.landoftherooster.R;
import com.playposse.landoftherooster.contentprovider.room.datahandler.ProductionCycleUtil;
import com.playposse.landoftherooster.contentprovider.room.datahandler.RoosterDaoUtil;
import com.playposse.landoftherooster.contentprovider.room.entity.Resource;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceType;
import com.playposse.landoftherooster.contentprovider.room.entity.ResourceWithType;
import com.playposse.landoftherooster.contentprovider.room.entity.UnitType;
import com.playposse.landoftherooster.util.GridLayoutRowViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * A dialog that allows a developer to change the game instantly for testing, e.g. create new units
 * and resources.
 */
public class DevModeDialogFragment extends BaseDialogFragment {

    @BindView(R.id.unit_grid_layout) GridLayout unitGridLayout;
    @BindView(R.id.resource_grid_layout) GridLayout resourceGridLayout;

    private List<UnitTypeWithCount> unitTypeWithCounts;
    private List<ResourceWithType> resourceWithTypes;

    public DevModeDialogFragment() {
        super(R.layout.dialog_dev_mode);

        setShowReturnToMapButton(true);
    }

    public static DevModeDialogFragment newInstance() {
        return new DevModeDialogFragment();
    }

    @Override
    protected Long readArguments(Bundle savedInstanceState) {
        return null;
    }

    @Override
    protected void doInBackground(Context appContext) {
        loadResourceInfo();
        loadUnitInfo();
    }

    private void loadResourceInfo() {
        List<ResourceType> allResourceWithTypes = getDao().getAllResourceTypes();
        Map<Long, Integer> resourceMap = ProductionCycleUtil.getResourcesJoiningUser(getDao());
        resourceWithTypes = new ArrayList<>();

        for (ResourceType resourceType : allResourceWithTypes) {
            long resourceTypeId = resourceType.getId();
            Integer resourceAmount = null;

            if (resourceMap.containsKey(resourceTypeId)) {
                resourceAmount = resourceMap.get(resourceTypeId);
            }

            if (resourceAmount == null) {
                resourceAmount = 0;
            }

            Resource resource = new Resource(
                    resourceTypeId,
                    resourceAmount,
                    null);
            resourceWithTypes.add(new ResourceWithType(resource, resourceType));
        }

        Collections.sort(
                resourceWithTypes,
                new Comparator<ResourceWithType>() {
                    @Override
                    public int compare(ResourceWithType r1, ResourceWithType r2) {
                        return Long.compare(r1.getType().getId(), r2.getType().getId());
                    }
                });
    }

    private void loadUnitInfo() {
        List<UnitType> allUnitTypes = getDao().getAllUnitTypes();
        Map<Long, Integer> unitMap = ProductionCycleUtil.getUnitCountsJoiningUser(getDao());
        unitTypeWithCounts = new ArrayList<>();

        for (UnitType unitType : allUnitTypes) {
            long unitTypeId = unitType.getId();
            if (unitMap.containsKey(unitTypeId)) {
                unitTypeWithCounts.add(new UnitTypeWithCount(unitType, unitMap.get(unitTypeId)));
            } else {
                unitTypeWithCounts.add(new UnitTypeWithCount(unitType, 0));
            }
        }

        Collections.sort(
                unitTypeWithCounts,
                new Comparator<UnitTypeWithCount>() {
                    @Override
                    public int compare(UnitTypeWithCount u1, UnitTypeWithCount u2) {
                        return Long.compare(u1.getUnitType().getId(), u2.getUnitType().getId());
                    }
                });
    }

    @Override
    protected void onPostExecute() {
        // Create resource grid.
        ResourceRowViewHolder resourceRowViewHolder = new ResourceRowViewHolder();
        for (ResourceWithType resourceWithType : resourceWithTypes) {
            resourceRowViewHolder.apply(resourceWithType);
        }
        resourceGridLayout.invalidate();

        // Create unit grid.
        UnitRowViewHolder unitRowViewHolder = new UnitRowViewHolder();
        for (UnitTypeWithCount unitTypeWithCount : unitTypeWithCounts) {
            unitRowViewHolder.apply(unitTypeWithCount);
        }
        unitGridLayout.invalidate();
    }

    /**
     * A helper class to populate a single row in the {@link GridLayout}. One row represents a
     * unit or resource that is joining the user.
     */
    abstract class RowViewHolder<T> extends GridLayoutRowViewHolder<T> {

        @BindView(R.id.name_text_view) TextView nameTextView;
        @BindView(R.id.add_button) Button addButton;
        @BindView(R.id.remove_button) Button removeButton;
        @BindView(R.id.amount_text_view) TextView amountTextView;

        private RowViewHolder(GridLayout gridLayout) {
            super(gridLayout, R.layout.list_item_dev_mode);

            gridLayout.removeAllViews();
        }
    }

    /**
     * Builder for a row in the unit grid.
     */
    class UnitRowViewHolder extends RowViewHolder<UnitTypeWithCount> {

        private UnitRowViewHolder() {
            super(unitGridLayout);
        }

        @Override
        protected void populate(final UnitTypeWithCount unitTypeWithCount) {
            final UnitType unitType = unitTypeWithCount.getUnitType();
            int count = unitTypeWithCount.getCount();

            nameTextView.setText(unitType.getName());
            amountTextView.setText(Integer.toString(count));

            removeButton.setEnabled(count > 0);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reload(new Runnable() {
                        @Override
                        public void run() {
                            RoosterDaoUtil.creditUnit(
                                    getDao(),
                                    unitType.getId(),
                                    1,
                                    null);
                        }
                    });
                }
            });

            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reload(new Runnable() {
                        @Override
                        public void run() {
                            RoosterDaoUtil.creditUnit(
                                    getDao(),
                                    unitType.getId(),
                                    -1,
                                    null);
                        }
                    });
                }
            });
        }
    }

    /**
     * Builder for a row in the resource grid.
     */
    class ResourceRowViewHolder extends RowViewHolder<ResourceWithType> {

        private ResourceRowViewHolder() {
            super(resourceGridLayout);
        }

        @Override
        protected void populate(final ResourceWithType resourceWithType) {
            final ResourceType resourceType = resourceWithType.getType();
            Resource resource = resourceWithType.getResource();
            int count = (resource != null) ? resource.getAmount() : 0;

            nameTextView.setText(resourceType.getName());
            amountTextView.setText(Integer.toString(count));

            removeButton.setEnabled(count > 0);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reload(new Runnable() {
                        @Override
                        public void run() {
                            RoosterDaoUtil.creditResource(
                                    getDao(),
                                    resourceType.getId(),
                                    1,
                                    null);
                        }
                    });
                }
            });

            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reload(new Runnable() {
                        @Override
                        public void run() {
                            RoosterDaoUtil.creditResource(
                                    getDao(),
                                    resourceType.getId(),
                                    -1,
                                    null);
                        }
                    });
                }
            });
        }
    }

    /**
     * A data class that combines a {@link UnitType} and how many units the user has joining of that
     * type.
     */
    private class UnitTypeWithCount {

        private final UnitType unitType;
        private final int count;

        private UnitTypeWithCount(UnitType unitType, int count) {
            this.unitType = unitType;
            this.count = count;
        }

        public UnitType getUnitType() {
            return unitType;
        }

        public int getCount() {
            return count;
        }
    }
}
