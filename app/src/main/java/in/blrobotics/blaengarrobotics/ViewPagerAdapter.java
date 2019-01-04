package in.blrobotics.blaengarrobotics;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        try {
            return fragmentList.get(position);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getCount() {
        return fragmentList.toArray().length;
    }

    public void addFragment(Fragment fragment){
        fragmentList.add(fragment);
    }
}
