package com.example.flightticket.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flightticket.R;
import com.example.flightticket.network.ApiClient;
import com.example.flightticket.network.ApiService;
import com.example.flightticket.network.model.Price;
import com.example.flightticket.network.model.Ticket;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements TicketAdapter.TicketsAdapterListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String from = "DEL";
    private static final String to = "HYD";
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    TicketViewModel ticketViewModel;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Unbinder unbinder;

    ApiService apiService;
    TicketAdapter ticketAdapter;
    List<Ticket> ticketList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        ticketViewModel = ViewModelProviders.of(this).get(TicketViewModel.class);
        apiService = ApiClient.getClient().create(ApiService.class);
        ticketAdapter = new TicketAdapter(this, ticketList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(ticketAdapter);


        ticketViewModel.getTickets();

        ticketViewModel.ticketListMutableLiveData.observe(this, new Observer<List<Ticket>>() {

            @Override
            public void onChanged(List<Ticket> tickets) {
                ticketList.clear();
                ticketList.addAll(tickets);
                ticketAdapter.notifyDataSetChanged();
            }
        });

        ticketViewModel.ticketWithPriceMutableLiveData.observe(this, new Observer<Ticket>() {
            @Override
            public void onChanged(Ticket ticket) {
                int position = ticketList.indexOf(ticket);
                if (position == -1) {
                    // TODO - take action
                    // Ticket not found in the list
                    // This shouldn't happen
                    return;
                }
                ticketList.set(position, ticket);
                ticketAdapter.notifyDataSetChanged();
            }
        });


//        ConnectableObservable<List<Ticket>> ticketsObservable = getTickets(from, to).replay();
//
//
//        /**
//         * Fetching all tickets first
//         * Observable emits List<Ticket> at once
//         * All the items will be added to RecyclerView
//         * */
//        disposable.add(
//                ticketsObservable
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribeWith(new DisposableObserver<List<Ticket>>() {
//
//
//                            @Override
//                            public void onNext(List<Ticket> tickets) {
//                                ticketList.clear();
//                                ticketList.addAll(tickets);
//                                ticketAdapter.notifyDataSetChanged();
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                Log.v("Main Activity ", e.getMessage());
//                            }
//
//                            @Override
//                            public void onComplete() {
//
//                            }
//                        }));
//
//
//
//
//
//
//
//        /**
//         * Fetching individual ticket price
//         * First FlatMap converts single List<Ticket> to multiple emissions
//         * Second FlatMap makes HTTP call on each Ticket emission
//         * */
//        disposable.add(
//                ticketsObservable
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        /**
//                         * Converting List<Ticket> emission to single Ticket emissions
//                         * */
//                        .concatMap(new Function<List<Ticket>, ObservableSource<Ticket>>() {
//                            @Override
//                            public ObservableSource<Ticket> apply(List<Ticket> tickets) throws Exception {
//                                return Observable.fromIterable(tickets);
//                            }
//                        })
//                        /**
//                         * Fetching price on each Ticket emission
//                         * */
//                        .flatMap(new Function<Ticket, ObservableSource<Ticket>>() {
//                            @Override
//                            public ObservableSource<Ticket> apply(Ticket ticket) throws Exception {
//                                return getPriceObservable(ticket);
//                            }
//                        })
//                        .subscribeWith(new DisposableObserver<Ticket>() {
//                            @Override
//                            public void onNext(Ticket ticket) {
//                                int position = ticketList.indexOf(ticket);
//                                if (position == -1) {
//                                    // TODO - take action
//                                    // Ticket not found in the list
//                                    // This shouldn't happen
//                                    return;
//                                }
//                                ticketList.set(position, ticket);
//                                ticketAdapter.notifyDataSetChanged();
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                Log.v("ERror", e.getMessage());
//                            }
//
//                            @Override
//                            public void onComplete() {
//
//                            }
//                        })
//
//        );
//
//
//
//        // Calling connect to start emission
//        ticketsObservable.connect();
//
//    }
//
//
//    Observable<List<Ticket>> getTickets(String from, String to) {
//        return apiService.searchTickets(from, to)
//                .toObservable()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread());
//
//    }
//
//
//    /**
//     * Making Retrofit call to get single ticket price
//     * get price HTTP call returns Price object, but
//     * map() operator is used to change the return type to Ticket
//     */
//
//
//    private Observable<Ticket> getPriceObservable(Ticket ticket) {
//        return apiService.getPrice(ticket.getFlightNumber(), ticket.getFrom(), ticket.getTo())
//                .toObservable()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(new Function<Price, Ticket>() {
//                    @Override
//                    public Ticket apply(Price price) throws Exception {
//                        ticket.setPrice(price);
//                        return ticket;
//                    }
//                });
    }

    @Override
    public void onTicketSelected(Ticket ticket) {
        Toast.makeText(this, "you select ticket of :" + ticket.getAirline().getName() + " price : " + ticket.getPrice().getPrice(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        unbinder.unbind();
    }
}
